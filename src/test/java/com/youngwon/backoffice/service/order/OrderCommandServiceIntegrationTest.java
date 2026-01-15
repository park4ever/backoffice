package com.youngwon.backoffice.service.order;

import com.youngwon.backoffice.common.value.Money;
import com.youngwon.backoffice.domain.order.Order;
import com.youngwon.backoffice.domain.order.OrderItem;
import com.youngwon.backoffice.domain.order.OrderStatus;
import com.youngwon.backoffice.domain.order.SalesChannel;
import com.youngwon.backoffice.domain.product.Product;
import com.youngwon.backoffice.domain.product.ProductOption;
import com.youngwon.backoffice.domain.shop.Shop;
import com.youngwon.backoffice.dto.order.create.OrderCreateCommand;
import com.youngwon.backoffice.dto.order.create.OrderCreateItemCommand;
import com.youngwon.backoffice.dto.order.create.OrderCreateWithItemsCommand;
import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import com.youngwon.backoffice.repository.order.OrderItemRepository;
import com.youngwon.backoffice.repository.order.OrderRepository;
import com.youngwon.backoffice.repository.product.ProductOptionRepository;
import com.youngwon.backoffice.repository.product.ProductRepository;
import com.youngwon.backoffice.repository.shop.ShopRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class OrderCommandServiceIntegrationTest {

    @Autowired OrderCommandService orderCommandService;

    @Autowired ShopRepository shopRepository;
    @Autowired ProductRepository productRepository;
    @Autowired ProductOptionRepository productOptionRepository;

    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;

    @Autowired EntityManager em;

    @Test
    @DisplayName("create: 주문(DRAFT)과 주문상품이 생성되고, 중복 옵션은 수량 합산된다")
    void create_createsOrderAndItems_and_mergesQuantity() {
        //given
        Shop shop = shopRepository.save(Shop.create("테스트상점"));
        Product product = productRepository.save(Product.create(shop, "아메리카노"));
        ProductOption option = productOptionRepository.save(ProductOption.create(product, "온도", "ICE", 1500L, 100));

        OrderCreateCommand header = new OrderCreateCommand(
                shop.getId(),
                "20260115-000001",
                SalesChannel.MANUAL,
                null,
                LocalDateTime.of(2026, 1, 15, 10, 0),
                "홍길동",
                "01011112222",
                Money.of(3000L),
                Money.zero(),
                Money.zero(),
                Money.zero(),
                "memo"
        );

        OrderCreateWithItemsCommand cmd = new OrderCreateWithItemsCommand(
                header,
                List.of(
                        new OrderCreateItemCommand(option.getId(), 1),
                        new OrderCreateItemCommand(option.getId(), 2) //duplicate -> merge to 3
                )
        );

        //when
        Long orderId = orderCommandService.create(cmd);
        em.flush();
        em.clear();

        //then
        Order saved = orderRepository.findByShopIdAndId(shop.getId(), orderId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.DRAFT);

        List<OrderItem> items = orderItemRepository.findAllByShopIdAndOrderId(shop.getId(), orderId);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(3);
        assertThat(items.get(0).getProductNameSnapshot()).isEqualTo("아메리카노");
    }

    @Test
    @DisplayName("confirm: DRAFT 주문 확정 시 재고가 차감된다")
    void confirm_decreasesStock() {
        //given
        Shop shop = shopRepository.save(Shop.create("테스트상점"));
        Product product = productRepository.save(Product.create(shop, "라떼"));
        ProductOption option = productOptionRepository.save(ProductOption.create(product, "온도", "HOT", 2000L, 10));

        Long orderId = createOrderWithSingleItem(shop, option, 3);

        //when
        orderCommandService.confirm(shop.getId(), orderId);
        em.flush();
        em.clear();

        //then
        Order order = orderRepository.findByShopIdAndId(shop.getId(), orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        ProductOption reloaded = productOptionRepository.findById(option.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("confirm: 재고 부족이면 확정 실패(예외)")
    void confirm_failsWhenInsufficientStock() {
        //given
        Shop shop = shopRepository.save(Shop.create("테스트상점"));
        Product product = productRepository.save(Product.create(shop, "라떼"));
        ProductOption option = productOptionRepository.save(ProductOption.create(product, "온도", "HOT", 2000L, 2));

        Long orderId = createOrderWithSingleItem(shop, option, 3);

        //when & then
        assertThatThrownBy(() -> orderCommandService.confirm(shop.getId(), orderId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);

        //상태는 여전히 DRAFT (트랜잭션 롤백)
        em.flush();
        em.clear();
        Order order = orderRepository.findByShopIdAndId(shop.getId(), orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DRAFT);
    }

    @Test
    @DisplayName("cancel: DRAFT 주문 취소는 재고 변화가 없다")
    void cancel_draft_noStockChange() {
        //given
        Shop shop = shopRepository.save(Shop.create("테스트상점"));
        Product product = productRepository.save(Product.create(shop, "아메리카노"));
        ProductOption option = productOptionRepository.save(ProductOption.create(product, "온도", "ICE", 1500L, 10));

        Long orderId = createOrderWithSingleItem(shop, option, 3);

        //when
        orderCommandService.cancel(shop.getId(), orderId);
        em.flush();
        em.clear();

        //then
        Order order = orderRepository.findByShopIdAndId(shop.getId(), orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);

        ProductOption reloaded = productOptionRepository.findById(option.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("cancel: CONFIRMED 주문 취소는 재고가 복구된다")
    void cancel_confirmed_restoresStock() {
        //given
        Shop shop = shopRepository.save(Shop.create("테스트상점"));
        Product product = productRepository.save(Product.create(shop, "아메리카노"));
        ProductOption option = productOptionRepository.save(ProductOption.create(product, "온도", "ICE", 1500L, 10));

        Long orderId = createOrderWithSingleItem(shop, option, 3);
        orderCommandService.confirm(shop.getId(), orderId);

        em.flush();
        em.clear();

        //when
        orderCommandService.cancel(shop.getId(), orderId);
        em.flush();
        em.clear();

        //then
        Order order = orderRepository.findByShopIdAndId(shop.getId(), orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);

        ProductOption reloaded = productOptionRepository.findById(option.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("refund: CONFIRMED 주문만 환불 가능하고 refundAmount가 저장된다")
    void refund_onlyConfirmed() {
        //given
        Shop shop = shopRepository.save(Shop.create("테스트상점"));
        Product product = productRepository.save(Product.create(shop, "라떼"));
        ProductOption option = productOptionRepository.save(ProductOption.create(product, "온도", "HOT", 2000L, 10));

        Long orderId = createOrderWithSingleItem(shop, option, 1);

        //DRAFT에서 환불 시도 -> 실패
        assertThatThrownBy(() -> orderCommandService.refund(shop.getId(), orderId, Money.of(1000)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_INVALID_STATE);

        //CONFIRMED로 만들고 환불
        orderCommandService.confirm(shop.getId(), orderId);
        orderCommandService.refund(shop.getId(), orderId, Money.of(1000));

        em.flush();
        em.clear();

        Order order = orderRepository.findByShopIdAndId(shop.getId(), orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
        assertThat(order.getRefundAmount().getAmount()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("changeFees: 취소 주문은 변경 불가")
    void changeFees_canceledForbidden() {
        //given
        Shop shop = shopRepository.save(Shop.create("테스트상점"));
        Product product = productRepository.save(Product.create(shop, "라떼"));
        ProductOption option = productOptionRepository.save(ProductOption.create(product, "온도", "HOT", 2000L, 10));
        Long orderId = createOrderWithSingleItem(shop, option, 1);

        orderCommandService.cancel(shop.getId(), orderId);

        //when & then
        assertThatThrownBy(() -> orderCommandService.changeFees(
                shop.getId(), orderId, Money.of(100), Money.of(100), Money.of(100)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_CANNOT_MODIFY_CANCELED);
    }

    @Test
    @DisplayName("changeMemo: 공백 메모는 null로 정규화된다")
    void changeMemo_blankToNull() {
        //given
        Shop shop = shopRepository.save(Shop.create("테스트상점"));
        Product product = productRepository.save(Product.create(shop, "라떼"));
        ProductOption option = productOptionRepository.save(ProductOption.create(product, "온도", "HOT", 2000L, 10));
        Long orderId = createOrderWithSingleItem(shop, option, 1);

        //when
        orderCommandService.changeMemo(shop.getId(), orderId, "   ");
        em.flush();
        em.clear();

        //then
        Order order = orderRepository.findByShopIdAndId(shop.getId(), orderId).orElseThrow();
        assertThat(order.getMemo()).isNull();
    }

    private Long createOrderWithSingleItem(Shop shop, ProductOption option, int qty) {
        OrderCreateCommand header = new OrderCreateCommand(
                shop.getId(),
                "20260115-999999",
                SalesChannel.MANUAL,
                null,
                LocalDateTime.of(2026, 1, 15, 10, 0),
                "테스터",
                "01000000000",
                Money.of(option.getPrice() * (long) qty),
                Money.zero(),
                Money.zero(),
                Money.zero(),
                null
        );

        OrderCreateWithItemsCommand cmd = new OrderCreateWithItemsCommand(
                header,
                List.of(new OrderCreateItemCommand(option.getId(), qty))
        );

        return orderCommandService.create(cmd);
}
}
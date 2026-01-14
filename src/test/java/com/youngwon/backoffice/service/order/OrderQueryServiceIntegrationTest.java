package com.youngwon.backoffice.service.order;

import com.youngwon.backoffice.common.value.Money;
import com.youngwon.backoffice.domain.order.*;
import com.youngwon.backoffice.domain.product.Product;
import com.youngwon.backoffice.domain.product.ProductOption;
import com.youngwon.backoffice.domain.shop.Shop;
import com.youngwon.backoffice.dto.order.OrderCreateCommand;
import com.youngwon.backoffice.dto.order.OrderListRowResponse;
import com.youngwon.backoffice.dto.order.OrderQueryCond;
import com.youngwon.backoffice.repository.ProductOptionRepository;
import com.youngwon.backoffice.repository.ProductRepository;
import com.youngwon.backoffice.repository.ShopRepository;
import com.youngwon.backoffice.repository.order.OrderItemRepository;
import com.youngwon.backoffice.repository.order.OrderRepository;
import com.youngwon.backoffice.service.OrderQueryService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderQueryServiceIntegrationTest {

    @Autowired OrderQueryService orderQueryService;

    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;

    @Autowired ProductRepository productRepository;
    @Autowired ProductOptionRepository productOptionRepository;
    @Autowired ShopRepository shopRepository;

    @Autowired EntityManager em;

    @Test
    @DisplayName("주문 리스트 : 대표상품명 + 외 N건 문구가 붙고, 기본은 최신순(orderedAt desc, id desc)이다.")
    void searchOrders_itemSummaryText_and_defaultSort() {
        //given
        Shop shop = shopRepository.save(Shop.create("testShop"));
        Product product = productRepository.save(Product.create(shop, "A-americano"));

        ProductOption option = ProductOption.create(product, "HOT/ICE", "ICE", 1500L, 100);
        option = productOptionRepository.save(option);

        //오래된 주문(아이템 1개) -> itemSummaryText = "B-latte"
        Order older = persistOrderDraft(
                shop.getId(),
                "20260114-000001",
                SalesChannel.MANUAL,
                "EXT-001",
                LocalDateTime.of(2026, 1, 14, 10, 0),
                "홍길동",
                "01012345678",
                1500L
        );
        orderItemRepository.save(OrderItem.create(older, option, "B-latte", 1));

        //최신 주문(아이템 3개) -> "A-americano 외 2건"
        Order newer = persistOrderDraft(
                shop.getId(),
                "20260114-000002",
                SalesChannel.MANUAL,
                "EXT-002",
                LocalDateTime.of(2026, 1, 14, 12, 0),
                "김철수",
                "01099477456",
                4500L
        );
        orderItemRepository.save(OrderItem.create(newer, option, "A-americano", 1));
        orderItemRepository.save(OrderItem.create(newer, option, "A-americano", 1));
        orderItemRepository.save(OrderItem.create(newer, option, "A-americano", 1));

        em.flush();
        em.clear();

        //when
        OrderQueryCond cond = new OrderQueryCond();
        Page<OrderListRowResponse> page = orderQueryService.searchOrders(
                shop.getId(),
                cond,
                PageRequest.of(0, 10)
        );

        //then
        assertThat(page.getTotalElements()).isEqualTo(2);

        List<OrderListRowResponse> rows = page.getContent();

        //최신순 기본 정렬이므로 newer가 먼저
        assertThat(rows.get(0).orderNo()).isEqualTo("20260114-000002");
        assertThat(rows.get(0).itemSummaryText()).isEqualTo("A-americano 외 2건");

        assertThat(rows.get(1).orderNo()).isEqualTo("20260114-000001");
        assertThat(rows.get(1).itemSummaryText()).isEqualTo("B-latte");
    }

    @Test
    @DisplayName("주문 리스트 : status 필터가 적용된다.")
    void searchOrders_filterByStatus() {
        //given
        Shop shop = shopRepository.save(Shop.create("testShop"));
        Product product = productRepository.save(Product.create(shop, "A-americano"));

        ProductOption option = productOptionRepository.save(
                ProductOption.create(product, "HOT/ICE", "ICE", 1500L, 100)
        );

        Order draft = persistOrderDraft(
                shop.getId(), "20260114-000010", SalesChannel.MANUAL, "EXT-010",
                LocalDateTime.of(2026, 1, 14, 9, 0),
                "홍길동", "01012345678", 1500L
        );
        orderItemRepository.save(OrderItem.create(draft, option, "A-americano", 1));

        Order confirmed = persistOrderDraft(
                shop.getId(), "20260114-000011", SalesChannel.MANUAL, "EXT-011",
                LocalDateTime.of(2026, 1, 14, 10, 0),
                "김철수", "01099477456", 1500L
        );
        orderItemRepository.save(OrderItem.create(confirmed, option, "A-americano", 1));
        confirmed.confirm(); // DRAFT -> CONFIRMED

        em.flush();
        em.clear();

        //when
        OrderQueryCond cond = new OrderQueryCond();
        cond.setStatus(OrderStatus.CONFIRMED);

        Page<OrderListRowResponse> page = orderQueryService.searchOrders(
                shop.getId(),
                cond,
                PageRequest.of(0, 10)
        );

        //then
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).orderNo()).isEqualTo("20260114-000011");
        assertThat(page.getContent().get(0).status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("주문 리스트 : 키워드 타입 선택형 검색이 동작한다.")
    void searchOrders_keywordType() {
        // given
        Shop shop = shopRepository.save(Shop.create("testShop"));
        Product product = productRepository.save(Product.create(shop, "A-americano"));
        ProductOption option = productOptionRepository.save(
                ProductOption.create(product, "HOT/ICE", "ICE", 1500L, 100)
        );

        Order order = persistOrderDraft(
                shop.getId(),
                "20260114-000100",
                SalesChannel.MANUAL,
                "COUPANG-EXT-100",
                LocalDateTime.of(2026, 1, 14, 11, 0),
                "박영원",
                "01021393389",
                1500L
        );
        orderItemRepository.save(OrderItem.create(order, option, "A-americano", 1));

        em.flush();
        em.clear();

        // ORDER_NO
        OrderQueryCond c1 = new OrderQueryCond();
        c1.setKeywordType(OrderKeywordType.ORDER_NO);
        c1.setKeyword("000100");
        assertOne(shop.getId(), c1, "20260114-000100");

        // EXTERNAL_REF
        OrderQueryCond c2 = new OrderQueryCond();
        c2.setKeywordType(OrderKeywordType.EXTERNAL_REF);
        c2.setKeyword("COUPANG-EXT");
        assertOne(shop.getId(), c2, "20260114-000100");

        // CUSTOMER_PHONE
        OrderQueryCond c3 = new OrderQueryCond();
        c3.setKeywordType(OrderKeywordType.CUSTOMER_PHONE);
        c3.setKeyword("0102139");

        assertOne(shop.getId(), c3, "20260114-000100");

        // CUSTOMER_NAME
        OrderQueryCond c4 = new OrderQueryCond();
        c4.setKeywordType(OrderKeywordType.CUSTOMER_NAME);
        c4.setKeyword("박영원");
        assertOne(shop.getId(), c4, "20260114-000100");
    }

    private void assertOne(Long shopId, OrderQueryCond cond, String expectedOrderNo) {
        Page<OrderListRowResponse> page = orderQueryService.searchOrders(shopId, cond, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).orderNo()).isEqualTo(expectedOrderNo);
    }

    private Order persistOrderDraft(
            Long shopId,
            String orderNo,
            SalesChannel channel,
            String externalRef,
            LocalDateTime orderedAt,
            String customerName,
            String customerPhone,
            long grossAmount
    ) {
        OrderCreateCommand cmd = new OrderCreateCommand(
                shopId,
                orderNo,
                channel,
                externalRef,
                orderedAt,
                customerName,
                customerPhone,
                Money.of(grossAmount),
                Money.zero(),
                Money.zero(),
                Money.zero(),
                null
        );
        Order order = Order.draft(cmd);
        orderRepository.save(order);
        return order;
    }
}

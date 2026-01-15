package com.youngwon.backoffice.service.order.impl;

import com.youngwon.backoffice.common.value.Money;
import com.youngwon.backoffice.domain.order.Order;
import com.youngwon.backoffice.domain.order.OrderItem;
import com.youngwon.backoffice.domain.product.ProductOption;
import com.youngwon.backoffice.domain.product.ProductOptionStatus;
import com.youngwon.backoffice.dto.order.create.OrderCreateCommand;
import com.youngwon.backoffice.dto.order.create.OrderCreateItemCommand;
import com.youngwon.backoffice.dto.order.create.OrderCreateWithItemsCommand;
import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import com.youngwon.backoffice.repository.order.OrderItemRepository;
import com.youngwon.backoffice.repository.order.OrderRepository;
import com.youngwon.backoffice.repository.product.ProductOptionRepository;
import com.youngwon.backoffice.service.order.OrderCommandService;
import com.youngwon.backoffice.service.order.processor.OrderCancelProcessor;
import com.youngwon.backoffice.service.order.processor.OrderConfirmProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.youngwon.backoffice.domain.product.ProductOptionStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductOptionRepository productOptionRepository;

    private final OrderConfirmProcessor orderConfirmProcessor;
    private final OrderCancelProcessor orderCancelProcessor;

    @Override
    public Long create(OrderCreateWithItemsCommand command) {
        if (command == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "command가 null입니다.");
        }
        OrderCreateCommand orderCmd = command.order();
        if (orderCmd == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "order command가 null입니다.");
        }
        if (command.items() == null || command.items().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "주문 상품은 1개 이상이어야 합니다.");
        }

        Long shopId = orderCmd.shopId();
        if (shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "shopId 값이 누락되었습니다.");
        }

        //아이템의 옵션 ID 수집
        Map<Long, Integer> optionQtyMap = mergeQuantities(command.items());

        //옵션 로딩
        List<Long> optionIds = new ArrayList<>(optionQtyMap.keySet());
        List<ProductOption> options = productOptionRepository.findAllByShopIdAndIdIn(shopId, optionIds);

        if (options.size() != optionIds.size()) {
            //누락된 옵션 찾기
            Set<Long> foundIds = options.stream().map(ProductOption::getId).collect(Collectors.toSet());
            List<Long> missing = optionIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 옵션이 포함되어 있습니다. optionIds = " + missing);
        }

        Map<Long, ProductOption> optionMap = options.stream()
                .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

        //Order 생성(DRAFT)
        Order order = Order.draft(orderCmd);
        orderRepository.save(order);

        //OrderITem 생성
        List<OrderItem> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : optionQtyMap.entrySet()) {
            Long optionId = e.getKey();
            Integer qty = e.getValue();

            ProductOption option = optionMap.get(optionId);

            //옵션 상태 검증
            if (option.getStatus() != ACTIVE) {
                throw new BusinessException(
                        ErrorCode.CONFLICT, "비활성 옵션은 주문에 담을 수 없습니다. optionId = " + optionId
                );
            }

            String productNameSnapshot = option.getProduct().getName();

            OrderItem orderItem = OrderItem.create(order, option, productNameSnapshot, qty);
            items.add(orderItem);
        }

        orderItemRepository.saveAll(items);

        return order.getId();
    }

    private static Map<Long, Integer> mergeQuantities(List<OrderCreateItemCommand> itemCommands) {
        Map<Long, Integer> map = new LinkedHashMap<>();
        for (OrderCreateItemCommand c : itemCommands) {
            if (c == null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "items에 null값이 포함되어 있습니다.");
            }
            if (c.productOptionId() == null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "productOptionId 값이 누락되었습니다.");
            }
            if (c.quantity() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "quantity는 1 이상이어야 합니다. quantity =" + c.quantity());
            }
            map.merge(c.productOptionId(), c.quantity(), Math::addExact);
        }
        return map;
    }

    @Override
    public void confirm(Long shopId, Long orderId) {
        orderConfirmProcessor.confirm(shopId, orderId);
    }

    @Override
    public void cancel(Long shopId, Long orderId) {
        orderCancelProcessor.cancel(shopId, orderId);
    }

    @Override
    public void refund(Long shopId, Long orderId, Money refundAmount) {
        Order order = findOrder(shopId, orderId);
        order.refund(refundAmount);
    }

    @Override
    public void changeFees(Long shopId, Long orderId, Money platformFeeAmount, Money paymentFeeAmount, Money otherDeductionAmount) {
        Order order = findOrder(shopId, orderId);
        order.changeFees(platformFeeAmount, paymentFeeAmount, otherDeductionAmount);
    }

    @Override
    public void changeMemo(Long shopId, Long orderId, String memo) {
        Order order = findOrder(shopId, orderId);
        String normalized = normalizeMemo(memo);
        order.changeMemo(normalized);
    }

    private Order findOrder(Long shopId, Long orderId) {
        if (shopId == null || orderId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "shopId/orderId 값이 누락되었습니다.");
        }
        return orderRepository.findByShopIdAndId(shopId, orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "주문을 찾을 수 없습니다. orderId = " + orderId));
    }

    private static String normalizeMemo(String memo) {
        if (memo == null) return null;

        String trimmed = memo.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
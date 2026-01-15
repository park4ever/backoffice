package com.youngwon.backoffice.service.order.processor;

import com.youngwon.backoffice.domain.order.Order;
import com.youngwon.backoffice.domain.order.OrderItem;
import com.youngwon.backoffice.domain.order.OrderStatus;
import com.youngwon.backoffice.domain.product.ProductOption;
import com.youngwon.backoffice.domain.product.ProductOptionStatus;
import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import com.youngwon.backoffice.repository.order.OrderItemRepository;
import com.youngwon.backoffice.repository.order.OrderRepository;
import com.youngwon.backoffice.repository.product.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.youngwon.backoffice.domain.order.OrderStatus.*;
import static com.youngwon.backoffice.domain.product.ProductOptionStatus.*;

@Component
@RequiredArgsConstructor
public class OrderConfirmProcessor {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductOptionRepository productOptionRepository;

    @Transactional
    public void confirm(Long shopId, Long orderId) {
        if (shopId == null || orderId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "shopId/orderId 값이 누락되었습니다.");
        }

        Order order = orderRepository.findByShopIdAndId(shopId, orderId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "주문을 찾을 수 없습니다. orderId = " + orderId));

        //이미 CONFIRMED 상태라면 멱등 처리
        if (order.getStatus() == CONFIRMED) return;

        if (order.getStatus() != DRAFT) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATE,
                    "DRAFT 상태의 주문만 확정할 수 있습니다. currentStatus = " + order.getStatus());
        }

        //주문 아이템 로딩
        List<OrderItem> items = orderItemRepository.findAllByShopIdAndOrderId(shopId, orderId);
        if (items.isEmpty()) {
            throw new BusinessException(ErrorCode.CONFLICT, "주문 상품이 없는 주문은 확정할 수 없습니다. orderId = " + orderId);
        }

        //옵션별 수량 합산
        Map<Long, Integer> qtyByOptionId = aggregateQty(items);

        //데드락 방지 : ids 정렬 + for update 락
        List<Long> optionIds = new ArrayList(qtyByOptionId.keySet());
        optionIds.sort(Long::compareTo);

        List<ProductOption> options = productOptionRepository.findAllByShopIdAndIdInForUpdate(shopId, optionIds);
        if (options.size() != optionIds.size()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "재고 차감 대상 옵션이 존재하지 않습니다. orderId = " + orderId);
        }

        //재고 검증 + 차감
        for (ProductOption option : options) {
            if (option.getStatus() != ACTIVE) {
                throw new BusinessException(ErrorCode.CONFLICT,
                        "비활성 옵션은 확정처리 할 수 없습니다. optionId = " + option.getId());
            }
            int qty = qtyByOptionId.getOrDefault(option.getId(), 0);
            if (qty <= 0) {
                continue;
            }
            //option 내부에서 부족하면 CONFLICT
            option.decreaseStock(qty);
        }
        //상태 전이
        order.confirm();
    }

    private static Map<Long, Integer> aggregateQty(List<OrderItem> items) {
        Map<Long, Integer> map = new HashMap<>();
        for (OrderItem item : items) {
            Long optionId = item.getProductOption().getId();
            map.merge(optionId, item.getQuantity(), Integer::sum);
        }
        return map;
    }
}
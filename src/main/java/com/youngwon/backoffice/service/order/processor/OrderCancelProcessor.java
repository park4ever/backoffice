package com.youngwon.backoffice.service.order.processor;

import com.youngwon.backoffice.domain.order.Order;
import com.youngwon.backoffice.domain.order.OrderItem;
import com.youngwon.backoffice.domain.order.OrderStatus;
import com.youngwon.backoffice.domain.product.ProductOption;
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

@Component
@RequiredArgsConstructor
public class OrderCancelProcessor {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductOptionRepository productOptionRepository;

    @Transactional
    public void cancel(Long shopId, Long orderId) {
        if (shopId == null || orderId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "shopId/orderId 값이 누락되었습니다.");
        }

        Order order = orderRepository.findByShopIdAndId(shopId, orderId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "주문을 찾을 수 없습니다. orderId = " + orderId));

        //이미 CANCELED면  멱등 처리
        if (order.getStatus() == CANCELED) return;

        //CONFIRMED일 때만 재고 복구
        boolean needRestoreStock = order.getStatus() == CONFIRMED;

        //상태 전이 규칙 검증
        if (order.getStatus() != DRAFT && order.getStatus() != CONFIRMED) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATE,
                    "DRAFT/CONFIRMED 상태의 주문만 취소할 수 있습니다. currentStatus = " + order.getStatus());
        }

        if (needRestoreStock) {
            List<OrderItem> items = orderItemRepository.findAllByShopIdAndOrderId(shopId, orderId);
            if (!items.isEmpty()) {
                Map<Long, Integer> qtyByOptionId = aggregateQty(items);

                List<Long> optionIds = new ArrayList<>(qtyByOptionId.keySet());
                optionIds.sort(Long::compareTo);

                List<ProductOption> options = productOptionRepository.findAllByShopIdAndIdInForUpdate(shopId, optionIds);
                if (options.size() != optionIds.size()) {
                    throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                            "재고 복구 대상 옵션이 존재하지 않습니다. orderId = " + orderId);
                }

                for (ProductOption option : options) {
                    Integer qty = qtyByOptionId.getOrDefault(option.getId(), 0);
                    if (qty <= 0) continue;
                    option.increaseStock(qty);
                }
            }
        }

        order.cancel();
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
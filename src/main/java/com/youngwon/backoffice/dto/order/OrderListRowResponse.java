package com.youngwon.backoffice.dto.order;

import com.youngwon.backoffice.domain.order.OrderStatus;
import com.youngwon.backoffice.domain.order.SalesChannel;

import java.time.LocalDateTime;

public record OrderListRowResponse(
        Long id,
        String orderNo,
        SalesChannel salesChannel,
        String externalRef,
        OrderStatus status,
        LocalDateTime orderedAt,
        String customerName,
        String customerPhone,
        long grossAmount,
        long deductionAmount,
        long settlementAmount,
        long refundAmount,
        String itemSummaryText
) {
    public static OrderListRowResponse of(OrderRow row, String itemSummaryText) {
        return new OrderListRowResponse(
                row.id(),
                row.orderNo(),
                row.salesChannel(),
                row.externalRef(),
                row.status(),
                row.orderedAt(),
                row.customerName(),
                row.customerPhone(),
                row.grossAmount(),
                row.deductionAmount(),
                row.settlementAmount(),
                row.refundAmount(),
                itemSummaryText
        );
    }
}
package com.youngwon.backoffice.dto.order.query;

public record OrderItemSummaryRow(
        Long orderId,
        String representativeProductName,
        Long itemCount
) {}
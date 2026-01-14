package com.youngwon.backoffice.dto.order;

public record OrderItemSummaryRow(
        Long orderId,
        String representativeProductName,
        long itemCount
) {}
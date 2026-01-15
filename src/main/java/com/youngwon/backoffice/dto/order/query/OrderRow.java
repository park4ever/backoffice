package com.youngwon.backoffice.dto.order.query;

import com.youngwon.backoffice.domain.order.OrderStatus;
import com.youngwon.backoffice.domain.order.SalesChannel;

import java.time.LocalDateTime;

public record OrderRow(
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
        long refundAmount
) {}
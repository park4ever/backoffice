package com.youngwon.backoffice.dto.order.create;

import com.youngwon.backoffice.common.value.Money;
import com.youngwon.backoffice.domain.order.SalesChannel;

import java.time.LocalDateTime;

public record OrderCreateCommand(
        Long shopId,
        String orderNo,
        SalesChannel salesChannel,
        String externalRef,
        LocalDateTime orderedAt,
        String customerName,
        String customerPhone,
        Money grossAmount,
        Money platformFeeAmount,
        Money paymentFeeAmount,
        Money otherDeductionAmount,
        String memo
) {}
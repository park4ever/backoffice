package com.youngwon.backoffice.dto.order.command;

import com.youngwon.backoffice.common.value.Money;
import jakarta.validation.constraints.NotNull;

public record OrderChangeFeesRequest(
        @NotNull Money platformFeeAmount,
        @NotNull Money paymentFeeAmount,
        @NotNull Money otherDeductionAmount
        ) {}
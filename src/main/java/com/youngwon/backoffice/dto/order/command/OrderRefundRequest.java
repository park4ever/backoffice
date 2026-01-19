package com.youngwon.backoffice.dto.order.command;

import com.youngwon.backoffice.common.value.Money;
import jakarta.validation.constraints.NotNull;

public record OrderRefundRequest(
        @NotNull Money refundAmount
        ) {}
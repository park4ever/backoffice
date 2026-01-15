package com.youngwon.backoffice.dto.order.create;

import com.youngwon.backoffice.common.value.Money;
import com.youngwon.backoffice.domain.order.SalesChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreateRequest(
        @NotBlank String orderNo,
        @NotNull SalesChannel salesChannel,
        String externalRef,
        @NotNull LocalDateTime orderedAt,
        @NotBlank String customerName,
        @NotBlank String customerPhone,

        @NotNull Money grossAmount,
        @NotNull Money platformFeeAmount,
        @NotNull Money paymentFeeAmount,
        @NotNull Money otherDeductionAmount,

        @Size(max = 500) String memo,

        @NotNull @Size(min = 1) List<@Valid OrderCreateItemRequest> items
) {}
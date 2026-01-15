package com.youngwon.backoffice.dto.order.create;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderCreateItemRequest(
        @NotNull Long productOptionId,
        @Min(1) int quantity
) {}
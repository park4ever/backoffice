package com.youngwon.backoffice.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductOptionAddRequest(
        @NotBlank @Size(max = 100) String optionName,
        @NotBlank @Size(max = 100) String optionValue,
        @Min(0) long price,
        @Min(0) int stockQuantity
) {}
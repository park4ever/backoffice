package com.youngwon.backoffice.dto.product;

public record ProductOptionSummaryResponse(
        Long optionId,
        String optionName,
        String optionValue,
        long price,
        int stockQuantity,
        String status,
        String skuKey,
        String skuLabel
) {}
package com.youngwon.backoffice.dto.product;

public record ProductOptionAddResponse(
        Long optionId,
        String skuKey,
        String skuLabel
) {}
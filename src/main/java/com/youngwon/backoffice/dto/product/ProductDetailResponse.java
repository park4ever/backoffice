package com.youngwon.backoffice.dto.product;

import java.util.List;

public record ProductDetailResponse(
        Long productId,
        String name,
        String status,
        List<ProductOptionSummaryResponse> options
) {}
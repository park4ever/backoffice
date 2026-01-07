package com.youngwon.backoffice.service;

import com.youngwon.backoffice.dto.product.ProductCreateRequest;
import com.youngwon.backoffice.dto.product.ProductDetailResponse;
import com.youngwon.backoffice.dto.product.ProductOptionAddRequest;
import com.youngwon.backoffice.dto.product.ProductOptionAddResponse;

public interface ProductService {

    Long createProduct(Long shopId, ProductCreateRequest request);

    ProductDetailResponse getProductDetail(Long shopId, Long productId);

    ProductOptionAddResponse addOption(Long shopId, Long productId, ProductOptionAddRequest request);

    void deactivateProduct(Long shopId, Long productId);

    void deactivateOption(Long shopId, Long optionId);
}
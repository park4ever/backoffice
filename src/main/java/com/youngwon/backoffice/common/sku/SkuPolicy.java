package com.youngwon.backoffice.common.sku;

public interface SkuPolicy {

    String createKey(Long shopId, Long productId, Long optionId);

    String createLabel(String optionName, String optionValue);
}
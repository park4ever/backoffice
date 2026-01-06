package com.youngwon.backoffice.common.sku;

import org.springframework.stereotype.Component;

@Component
public class DefaultSkuPolicy implements SkuPolicy {

    @Override
    public String createKey(Long shopId, Long productId, Long optionId) {
        //불변 + ASCII + 충돌 없게
        return "S" + shopId + "-P" + productId + "-O" + optionId;
    }

    @Override
    public String createLabel(String optionName, String optionValue) {
        //표시용 : 한글/특수문자 OK
        return optionName.trim() + "/" + optionValue.trim();
    }
}
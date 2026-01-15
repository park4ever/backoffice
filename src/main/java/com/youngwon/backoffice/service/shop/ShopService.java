package com.youngwon.backoffice.service.shop;

import com.youngwon.backoffice.domain.shop.Shop;

public interface ShopService {

    Long create(String name);

    Shop getById(Long shopId);
}
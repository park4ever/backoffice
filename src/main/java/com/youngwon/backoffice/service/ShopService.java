package com.youngwon.backoffice.service;

import com.youngwon.backoffice.domain.shop.Shop;

public interface ShopService {

    Long create(String name);

    Shop getById(Long shopId);
}
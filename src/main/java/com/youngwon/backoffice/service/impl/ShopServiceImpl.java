package com.youngwon.backoffice.service.impl;

import com.youngwon.backoffice.domain.shop.Shop;
import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import com.youngwon.backoffice.repository.ShopRepository;
import com.youngwon.backoffice.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public Long create(String name) {
        Shop shop = Shop.create(name);
        shopRepository.save(shop);
        return shop.getId();
    }

    @Override
    public Shop getById(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
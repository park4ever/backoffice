package com.youngwon.backoffice.repository.shop;

import com.youngwon.backoffice.domain.shop.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {
}
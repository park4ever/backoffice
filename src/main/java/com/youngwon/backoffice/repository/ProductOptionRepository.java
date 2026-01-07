package com.youngwon.backoffice.repository;

import com.youngwon.backoffice.domain.product.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    Optional<ProductOption> findByShopIdAndSkuKey(Long shopId, String skuKey);

    Optional<ProductOption> findByIdAndShopId(Long id, Long shopId);

    List<ProductOption> findAllByProductIdAndShopId(Long productId, Long shopId);

    boolean existsByProductIdAndOptionNameAndOptionValue(Long productId, String optionName, String optionValue);
}
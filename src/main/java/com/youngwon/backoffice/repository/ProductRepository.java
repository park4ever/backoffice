package com.youngwon.backoffice.repository;

import com.youngwon.backoffice.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndShopId(Long id, Long shopId);

    Page<Product> findAllByShopId(Long shopId, Pageable pageable);

    @EntityGraph(attributePaths = "options")
    Optional<Product> findWithOptionsByIdAndShopId(Long id, Long shopId);
}
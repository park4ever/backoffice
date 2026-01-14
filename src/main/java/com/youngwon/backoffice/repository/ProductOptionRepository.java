package com.youngwon.backoffice.repository;

import com.youngwon.backoffice.domain.product.ProductOption;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.*;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    Optional<ProductOption> findByShopIdAndSkuKey(Long shopId, String skuKey);

    Optional<ProductOption> findByIdAndShopId(Long id, Long shopId);

    List<ProductOption> findAllByProductIdAndShopId(Long productId, Long shopId);

    boolean existsByProductIdAndOptionNameAndOptionValue(Long productId, String optionName, String optionValue);

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
        select po
        from ProductOption po
        where po.shopId = :shopId
          and po.id in :ids
        order by po.id asc
    """)
    List<ProductOption> findAllByShopIdAndIdInForUpdate(@Param("shopId") Long shopId,
                                                        @Param("ids") List<Long> ids);
}
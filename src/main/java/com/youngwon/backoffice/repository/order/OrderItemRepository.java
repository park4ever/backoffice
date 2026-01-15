package com.youngwon.backoffice.repository.order;

import com.youngwon.backoffice.domain.order.OrderItem;
import com.youngwon.backoffice.dto.order.query.OrderItemSummaryRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByShopIdAndOrderId(Long shopId, Long orderId);

    boolean existsByShopIdAndOrderId(Long shopId, Long orderId);

    @Query("""
        select oi.productOption.id
        from OrderItem oi
        where oi.shopId = :shopId
          and oi.order.id = :orderId
    """)
    List<Long> findProductOptionIdsByShopIdAndOrderId(@Param("shopId") Long shopId,
                                                      @Param("orderId") Long orderId);

    @Query("""
        select new com.youngwon.backoffice.dto.order.query.OrderItemSummaryRow(
            oi.order.id,
            min(oi.productNameSnapshot),
            count(oi.id)
        )
        from OrderItem oi
        where oi.shopId = :shopId
          and oi.order.id in :orderIds
        group by oi.order.id
    """)
    List<OrderItemSummaryRow> summarizeByOrderIds(@Param("shopId") Long shopId,
                                                  @Param("orderIds") List<Long> orderIds);
}
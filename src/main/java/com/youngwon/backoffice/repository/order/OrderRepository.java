package com.youngwon.backoffice.repository.order;

import com.youngwon.backoffice.domain.order.Order;
import com.youngwon.backoffice.domain.order.SalesChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    Optional<Order> findByShopIdAndId(Long shopId, Long id);

    boolean existsByShopIdAndOrderNo(Long shopId, String orderNo);

    boolean existsByShopIdAndSalesChannelAndExternalRef(Long shopId, SalesChannel salesChannel, String externalRef);
}
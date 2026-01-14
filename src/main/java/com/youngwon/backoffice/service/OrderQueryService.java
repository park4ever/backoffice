package com.youngwon.backoffice.service;

import com.youngwon.backoffice.dto.order.OrderListRowResponse;
import com.youngwon.backoffice.dto.order.OrderQueryCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryService {
    Page<OrderListRowResponse> searchOrders(Long shopId, OrderQueryCond cond, Pageable pageable);
}
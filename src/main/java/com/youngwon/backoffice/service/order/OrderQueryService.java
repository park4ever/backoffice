package com.youngwon.backoffice.service.order;

import com.youngwon.backoffice.dto.order.query.OrderListRowResponse;
import com.youngwon.backoffice.dto.order.query.OrderQueryCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryService {
    Page<OrderListRowResponse> searchOrders(Long shopId, OrderQueryCond cond, Pageable pageable);
}
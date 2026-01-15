package com.youngwon.backoffice.repository.order;

import com.youngwon.backoffice.dto.order.query.OrderQueryCond;
import com.youngwon.backoffice.dto.order.query.OrderRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {
    Page<OrderRow> searchOrderRows(Long shopId, OrderQueryCond cond, Pageable pageable);
}
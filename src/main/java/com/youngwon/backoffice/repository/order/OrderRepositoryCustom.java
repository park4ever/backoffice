package com.youngwon.backoffice.repository.order;

import com.youngwon.backoffice.dto.order.OrderQueryCond;
import com.youngwon.backoffice.dto.order.OrderRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {
    Page<OrderRow> searchOrderRows(Long shopId, OrderQueryCond cond, Pageable pageable);
}
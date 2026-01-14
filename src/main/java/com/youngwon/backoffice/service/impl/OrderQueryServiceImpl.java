package com.youngwon.backoffice.service.impl;

import com.youngwon.backoffice.dto.order.OrderItemSummaryRow;
import com.youngwon.backoffice.dto.order.OrderListRowResponse;
import com.youngwon.backoffice.dto.order.OrderQueryCond;
import com.youngwon.backoffice.dto.order.OrderRow;
import com.youngwon.backoffice.repository.order.OrderItemRepository;
import com.youngwon.backoffice.repository.order.OrderRepository;
import com.youngwon.backoffice.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Page<OrderListRowResponse> searchOrders(Long shopId, OrderQueryCond cond, Pageable pageable) {
        Page<OrderRow> page = orderRepository.searchOrderRows(shopId, cond, pageable);

        List<OrderRow> rows = page.getContent();
        if (rows.isEmpty()) {
            return page.map(row -> OrderListRowResponse.of(row, ""));
        }

        List<Long> orderIds = rows.stream().map(OrderRow::id).toList();

        Map<Long, OrderItemSummaryRow> summaryMap = orderItemRepository
                .summarizeByOrderIds(shopId, orderIds)
                .stream()
                .collect(Collectors.toMap(OrderItemSummaryRow::orderId, Function.identity()));

        return page.map(row -> {
            OrderItemSummaryRow summary = summaryMap.get(row.id());
            String text = buildItemSummaryText(summary);
            return OrderListRowResponse.of(row, text);
        });
    }

    private String buildItemSummaryText(OrderItemSummaryRow summary) {
        if (summary == null) {
            return ""; //데이터 불일치 시 방어
        }

        String name = summary.representativeProductName();
        long count = summary.itemCount();

        if (count <= 0) {
            return "";
        }
        if (count == 1) {
            return name;
        }
        return name + " 외 " + (count - 1) + "건";
    }
}
package com.youngwon.backoffice.dto.order.mapper;

import com.youngwon.backoffice.common.auth.LoginUser;
import com.youngwon.backoffice.dto.order.create.OrderCreateCommand;
import com.youngwon.backoffice.dto.order.create.OrderCreateItemCommand;
import com.youngwon.backoffice.dto.order.create.OrderCreateRequest;
import com.youngwon.backoffice.dto.order.create.OrderCreateWithItemsCommand;

import java.util.List;

public final class OrderCommandMapper {

    private OrderCommandMapper() {}

    public static OrderCreateWithItemsCommand toCreateWithItemsCommand(LoginUser user, OrderCreateRequest request) {
        OrderCreateCommand order = new OrderCreateCommand(
                user.shopId(),
                request.orderNo(),
                request.salesChannel(),
                request.externalRef(),
                request.orderedAt(),
                request.customerName(),
                request.customerPhone(),
                request.grossAmount(),
                request.platformFeeAmount(),
                request.paymentFeeAmount(),
                request.otherDeductionAmount(),
                request.memo()
        );

        List<OrderCreateItemCommand> items = request.items().stream()
                .map(i -> new OrderCreateItemCommand(i.productOptionId(), i.quantity()))
                .toList();

        return new OrderCreateWithItemsCommand(order, items);
    }
}
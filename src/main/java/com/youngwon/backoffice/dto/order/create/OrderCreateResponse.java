package com.youngwon.backoffice.dto.order.create;

public record OrderCreateResponse(Long orderId) {
    public static OrderCreateResponse of(Long orderId) {
        return new OrderCreateResponse(orderId);
    }
}
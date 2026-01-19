package com.youngwon.backoffice.controller.api.order;

import com.youngwon.backoffice.common.auth.CurrentUser;
import com.youngwon.backoffice.common.auth.LoginUser;
import com.youngwon.backoffice.dto.order.command.OrderChangeFeesRequest;
import com.youngwon.backoffice.dto.order.command.OrderChangeMemoRequest;
import com.youngwon.backoffice.dto.order.command.OrderRefundRequest;
import com.youngwon.backoffice.dto.order.create.*;
import com.youngwon.backoffice.dto.order.mapper.OrderCommandMapper;
import com.youngwon.backoffice.service.order.OrderCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderCommandApiController {

    private final OrderCommandService orderCommandService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> create(@CurrentUser LoginUser user,
                                                      @Valid @RequestBody OrderCreateRequest request) {

        Long orderId = orderCommandService.create(OrderCommandMapper.toCreateWithItemsCommand(user, request));
        return ResponseEntity.ok(OrderCreateResponse.of(orderId));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Void> confirm(@CurrentUser LoginUser user,
                                        @PathVariable("id") Long orderId) {
        orderCommandService.confirm(user.shopId(), orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@CurrentUser LoginUser user,
                                       @PathVariable("id") Long orderId) {
        orderCommandService.cancel(user.shopId(), orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Void> refund(@CurrentUser LoginUser user,
                                       @PathVariable("id") Long orderId,
                                       @Valid @RequestBody OrderRefundRequest request) {
        orderCommandService.refund(user.shopId(), orderId, request.refundAmount());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/fees")
    public ResponseEntity<Void> changeFees(@CurrentUser LoginUser user,
                                           @PathVariable("id") Long orderId,
                                           @Valid @RequestBody OrderChangeFeesRequest request) {
        orderCommandService.changeFees(
                user.shopId(),
                orderId,
                request.platformFeeAmount(),
                request.paymentFeeAmount(),
                request.otherDeductionAmount()
        );
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/memo")
    public ResponseEntity<Void> changeMemo(@CurrentUser LoginUser user,
                                           @PathVariable("id") Long orderId,
                                           @Valid @RequestBody OrderChangeMemoRequest request) {
        orderCommandService.changeMemo(user.shopId(), orderId, request.memo());
        return ResponseEntity.ok().build();
    }
}
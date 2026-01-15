package com.youngwon.backoffice.controller.api.order;

import com.youngwon.backoffice.common.auth.CurrentUser;
import com.youngwon.backoffice.common.auth.LoginUser;
import com.youngwon.backoffice.dto.order.query.OrderListRowResponse;
import com.youngwon.backoffice.dto.order.query.OrderQueryCond;
import com.youngwon.backoffice.service.order.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderQueryApiController {

    private final OrderQueryService orderQueryService;

    @GetMapping
    public Page<OrderListRowResponse> list(@CurrentUser LoginUser user,
                                           OrderQueryCond cond, Pageable pageable) {
        return orderQueryService.searchOrders(user.shopId(), cond, pageable);
    }
}
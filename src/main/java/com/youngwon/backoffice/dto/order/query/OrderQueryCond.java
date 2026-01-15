package com.youngwon.backoffice.dto.order.query;

import com.youngwon.backoffice.domain.order.OrderKeywordType;
import com.youngwon.backoffice.domain.order.OrderStatus;
import com.youngwon.backoffice.domain.order.SalesChannel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
//웹 바인딩까지 고려하여 record 타입이 아닌 POJO 선택
public class OrderQueryCond {

    private LocalDateTime from;             //orderedAt >= from
    private LocalDateTime to;               //orderedAt < to
    private OrderStatus status;             //nullable
    private SalesChannel salesChannel;      //nullable

    private OrderKeywordType keywordType;   //nullable
    private String keyword;                 //nullable

    //정렬 방향 : true -> ASC, false -> DESC
    //(정렬은 orderedAt만 허용하고 id는 타이브레이커로 같은 방향 강제)
    private boolean orderedAtAsc = false;
}
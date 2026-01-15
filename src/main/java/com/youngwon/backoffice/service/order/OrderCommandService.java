package com.youngwon.backoffice.service.order;

import com.youngwon.backoffice.common.value.Money;
import com.youngwon.backoffice.dto.order.create.OrderCreateWithItemsCommand;

public interface OrderCommandService {

    /**
     * 주문 등록(수기/CSV/플랫폼 연동 공통)
     * - Order(DRAFT) 생성 + OrderItem 생성
     * - 아직 재고 차감은 하지 않음(확정 시 차감)
     */
    Long create(OrderCreateWithItemsCommand command);

    /**
     * 주문 확정
     * - DRAFT -> CONFIRMED
     * - OrderItem 기준으로 ProductOption 재고 차감(비관적 락)
     */
    void confirm(Long shopId, Long orderId);

    /**
     * 주문 취소
     * - DRAFT/CONFIRMED -> CANCELED
     * - CONFIRMED 상태에서 취소 시, 재고 복구(비관적 락)
     */
    void cancel(Long shopId, Long orderId);

    /**
     * 환불 처리
     * - CONFIRMED -> REFUNDED
     * - refundAmount만 기록(정산금액 재계산 없음 : refundAmount로 분리해서 관리)
     */
    void refund(Long shopId, Long orderId, Money refundAmount);

    /**
     * 수수료/공제 변경
     * - 취소 주문은 변경 불가
     * - 변경 시, 정산 금액 재계산
     */
    void changeFees(Long shopId,
                    Long orderId,
                    Money platformFeeAmount,
                    Money paymentFeeAmount,
                    Money otherDeductionAmount);

    /**
     * 메모 수정(운영 편의)
     */
    void changeMemo(Long shopId, Long orderId, String memo);
}
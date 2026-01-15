package com.youngwon.backoffice.domain.order;

import com.youngwon.backoffice.common.entity.BaseTimeEntity;
import com.youngwon.backoffice.common.value.Money;
import com.youngwon.backoffice.dto.order.create.OrderCreateCommand;
import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.youngwon.backoffice.domain.order.OrderStatus.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_shop_id_order_no", columnNames = {"shop_id", "order_no"}),
                @UniqueConstraint(name = "uk_orders_shop_id_channel_external_ref", columnNames = {"shop_id", "sales_channel", "external_ref"})
        },
        indexes = {
                @Index(name = "idx_orders_shop_id_ordered_at", columnList = "shop_id, ordered_at"),
                @Index(name = "idx_orders_shop_id_status_ordered_at", columnList = "shop_id, status, ordered_at")
        }
)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "order_no", nullable = false, length = 30)
    private String orderNo;

    @Enumerated(STRING)
    @Column(name = "sales_channel", nullable = false, length = 20)
    private SalesChannel salesChannel;

    /**
     * 플랫폼 주문번호 / CSV row key 등 외부 원천 식별자
     * - MANUAL 주문은 null 허용
     * - CSV/플랫폼 주문은 반드시 채우기
     */
    @Column(name = "external_ref", length = 100)
    private String externalRef;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 30)
    private String customerPhone;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "gross_amount", nullable = false))
    private Money grossAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "platform_fee_amount", nullable = false))
    private Money platformFeeAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "payment_fee_amount", nullable = false))
    private Money paymentFeeAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "other_deduction_amount", nullable = false))
    private Money otherDeductionAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "deduction_amount", nullable = false))
    private Money deductionAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "settlement_amount", nullable = false))
    private Money settlementAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "refund_amount", nullable = false))
    private Money refundAmount;

    @Column(name = "memo", length = 500)
    private String memo;

    public static Order draft(OrderCreateCommand cmd) {
        if (cmd == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "command가 null입니다.");
        }

        Order order = new Order();
        order.shopId = requireNonNull(cmd.shopId(), "shopId");
        order.orderNo = requireText(cmd.orderNo(), "orderNo");
        validateOrderNo(order.orderNo);

        order.salesChannel = requireNonNull(cmd.salesChannel(), "salesChannel");
        order.externalRef = cmd.externalRef();

        order.orderedAt = requireNonNull(cmd.orderedAt(), "orderedAt");

        order.customerName = requireText(cmd.customerName(), "customerName");
        order.customerPhone = requireText(cmd.customerPhone(), "customerPhone");

        order.grossAmount = requireNonNull(cmd.grossAmount(), "grossAmount");
        order.platformFeeAmount = requireNonNull(cmd.platformFeeAmount(), "platformFeeAmount");
        order.paymentFeeAmount = requireNonNull(cmd.paymentFeeAmount(), "paymentFeeAmount");
        order.otherDeductionAmount = requireNonNull(cmd.otherDeductionAmount(), "otherDeductionAmount");

        order.refundAmount = Money.zero();
        order.memo = cmd.memo();
        order.status = OrderStatus.DRAFT;

        if (order.salesChannel != SalesChannel.MANUAL) {
            order.externalRef = requireText(order.externalRef, "externalRef");
        }

        order.recalculateSettlementAmounts();
        return order;
    }

    public void confirm() {
        if (this.status != DRAFT) {
            throw new BusinessException(
                    ErrorCode.ORDER_INVALID_STATE,
                    "DRAFT 상태의 주문만 확정할 수 있습니다. currentStatus = " + this.status
            );
        }
        this.status = CONFIRMED;
    }

    public void cancel() {
        if (this.status != DRAFT && this.status != CONFIRMED) {
            throw new BusinessException(
                    ErrorCode.ORDER_INVALID_STATE,
                    "DRAFT/CONFIRMED 상태의 주문만 취소할 수 있습니다. currentStatus = " + this.status
            );
        }
        this.status = CANCELED;
    }

    public void refund(Money refundAmount) {
        requireNonNull(refundAmount, "refundAmount");

        if (this.status != CONFIRMED) {
            throw new BusinessException(
                    ErrorCode.ORDER_INVALID_STATE,
                    "CONFIRMED 상태의 주문만 환불 처리할 수 있습니다. currentStatus =" + this.status
            );
        }

        if (refundAmount.getAmount() > this.grossAmount.getAmount()) {
            throw new BusinessException(
                    ErrorCode.ORDER_AMOUNT_INVALID,
                    "환불액은 매출총액을 초과할 수 없습니다. gross = " + grossAmount.getAmount() + ", refund = " + refundAmount.getAmount()
            );
        }

        this.refundAmount = refundAmount;
        this.status = REFUNDED;
    }

    public void changeFees(Money platformFeeAmount, Money paymentFeeAmount, Money otherDeductionAmount) {
        if (this.status == CANCELED) {
            throw new BusinessException(
                    ErrorCode.ORDER_CANNOT_MODIFY_CANCELED,
                    "취소된 주문은 수수료/공제 항목을 변경할 수 없습니다. orderId = " + this.id
            );
        }

        this.platformFeeAmount = requireNonNull(platformFeeAmount, "platformFeeAmount");
        this.paymentFeeAmount = requireNonNull(paymentFeeAmount, "paymentFeeAmount");
        this.otherDeductionAmount = requireNonNull(otherDeductionAmount, "otherDeductionAmount");

        recalculateSettlementAmounts();
    }

    public void changeMemo(String memo) {
        if (memo != null && memo.length() > 500) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "메모는 500자를 초과할 수 없습니다.");
        }
        this.memo = memo;
    }

    private void recalculateSettlementAmounts() {
        //deduction = platform + payment + other
        this.deductionAmount = this.platformFeeAmount
                .plus(this.paymentFeeAmount)
                .plus(this.otherDeductionAmount);

        if (this.deductionAmount.getAmount() > this.grossAmount.getAmount()) {
            throw new BusinessException(
                    ErrorCode.ORDER_SETTLEMENT_NEGATIVE,
                    "정산 금액이 0원 미만입니다. gross =" + grossAmount.getAmount() + ", deduction = " + deductionAmount.getAmount()
            );
        }

        this.settlementAmount = this.grossAmount.minus(this.deductionAmount);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new BusinessException(
                    ErrorCode.ORDER_REQUIRED_FIELD_MISSING, field + " 값이 누락되었습니다.");
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    ErrorCode.ORDER_REQUIRED_FIELD_MISSING, field + " 값이 누락되었습니다.");
        }
        return value;
    }

    private static void validateOrderNo(String orderNo) {
        // YYYYMMDD-000001 형태 (8자리 날짜 + '-' + 6자리 숫자)
        if (!orderNo.matches("^\\d{8}-\\d{6}$")) {
            throw new BusinessException(ErrorCode.ORDER_ORDER_NO_INVALID, "orderNo=" + orderNo);
        }
    }
}
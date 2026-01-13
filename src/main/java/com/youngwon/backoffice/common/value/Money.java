package com.youngwon.backoffice.common.value;

import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.*;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public class Money {

    @Column(name = "amount", nullable = false)
    private long amount;

    private Money(long amount) {
        if (amount < 0) {
            throw new BusinessException(
                    ErrorCode.NEGATIVE_MONEY_NOT_ALLOWED, "amount=" + amount);
        }
        this.amount = amount;
    }

    public static Money zero() {
        return new Money(0L);
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    public Money plus(Money other) {
        if (other == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "other가 null 입니다.");
        }
        try {
            return new Money(Math.addExact(this.amount, other.amount));
        } catch (ArithmeticException e) {
            throw new BusinessException(
                    ErrorCode.MONEY_OVERFLOW,
                    "Money.plus 오버플로우: left=" + this.amount + ", right=" + other.amount
            );
        }
    }

    public Money minus(Money other) {
        if (other == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST, "other가 null 입니다.");
        }

        final long result;
        try {
            result = Math.subtractExact(this.amount, other.amount);
        } catch (ArithmeticException e) {
            throw new BusinessException(
                    ErrorCode.MONEY_OVERFLOW,
                    "Money.minus 오버플로우: left=" + this.amount + ", right=" + other.amount
            );
        }

        if (result < 0) {
            throw new BusinessException(
                    ErrorCode.MONEY_OPERATION_RESULT_NEGATIVE,
                    "Money.minus 결과가 음수입니다: left=" + this.amount + ", right=" + other.amount + ", result=" + result
            );
        }
        return new Money(result);
    }

    public boolean isZero() {
        return this.amount == 0L;
    }
}
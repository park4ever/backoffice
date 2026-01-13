package com.youngwon.backoffice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "요청이 충돌했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    NEGATIVE_MONEY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "금액은 0 이상이어야 합니다."),
    MONEY_OPERATION_RESULT_NEGATIVE(HttpStatus.BAD_REQUEST, "금액 계산 결과가 0 미만입니다."),
    MONEY_OVERFLOW(HttpStatus.INTERNAL_SERVER_ERROR, "금액 계산 중 오류가 발생했습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "주문 필수 값이 누락되었습니다."),
    ORDER_EXTERNAL_REF_REQUIRED(HttpStatus.BAD_REQUEST, "외부 주문 식별자는 필수입니다."),
    ORDER_ORDER_NO_INVALID(HttpStatus.BAD_REQUEST, "주문번호 형식이 올바르지 않습니다."),

    ORDER_DUPLICATE_ORDER_NO(HttpStatus.CONFLICT, "이미 존재하는 주문번호입니다."),
    ORDER_DUPLICATE_EXTERNAL_REF(HttpStatus.CONFLICT, "이미 등록된 외부 주문입니다."),

    ORDER_INVALID_STATE(HttpStatus.CONFLICT, "주문 상태가 올바르지 않습니다."),
    ORDER_CANNOT_MODIFY_CANCELED(HttpStatus.CONFLICT, "취소된 주문은 수정할 수 없습니다."),

    ORDER_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "주문 금액이 올바르지 않습니다."),
    ORDER_SETTLEMENT_NEGATIVE(HttpStatus.BAD_REQUEST, "정산 금액은 0 미만이 될 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
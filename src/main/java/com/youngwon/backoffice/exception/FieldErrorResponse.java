package com.youngwon.backoffice.exception;

public record FieldErrorResponse(
        String field,
        String reason
) {
    public static FieldErrorResponse of(String field, String reason) {
        return new FieldErrorResponse(field, reason);
    }
}
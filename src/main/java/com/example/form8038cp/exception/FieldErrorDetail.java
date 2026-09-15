package com.example.form8038cp.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FieldErrorDetail {
    private final String code;
    private final String field;
    private final String message;

    public static FieldErrorDetail of(String code, String field, String message) {
        return new FieldErrorDetail(code, field, message);
    }
}

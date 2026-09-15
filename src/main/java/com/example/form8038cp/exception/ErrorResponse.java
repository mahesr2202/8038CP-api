package com.example.form8038cp.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final String error;
    private final String message;
    private final List<FieldErrorDetail> errors;

    public static ErrorResponse of(ApiErrorCode code, String message, List<FieldErrorDetail> errors) {
        return new ErrorResponse(code.name(), message, errors);
    }
}

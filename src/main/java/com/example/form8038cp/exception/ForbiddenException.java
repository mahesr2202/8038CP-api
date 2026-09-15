package com.example.form8038cp.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, message, List.of());
    }
}

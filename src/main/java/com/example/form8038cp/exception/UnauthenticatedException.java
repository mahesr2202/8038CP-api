package com.example.form8038cp.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class UnauthenticatedException extends ApiException {
    public UnauthenticatedException(String message) {
        super(HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHENTICATED, message, List.of());
    }
}

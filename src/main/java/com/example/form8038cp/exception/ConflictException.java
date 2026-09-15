package com.example.form8038cp.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, message, List.of());
    }
}

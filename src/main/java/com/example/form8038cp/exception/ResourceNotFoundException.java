package com.example.form8038cp.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, message, List.of());
    }
}

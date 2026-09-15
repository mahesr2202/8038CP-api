package com.example.form8038cp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final ApiErrorCode code;
    private final List<FieldErrorDetail> errors;

    protected ApiException(HttpStatus status, ApiErrorCode code, String message, List<FieldErrorDetail> errors) {
        super(message);
        this.status = status;
        this.code = code;
        this.errors = errors != null ? errors : List.of();
    }
}

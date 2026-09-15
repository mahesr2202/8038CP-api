package com.example.form8038cp.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ValidationException extends ApiException {

    public ValidationException(String message, List<FieldErrorDetail> errors) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrorCode.VALIDATION_FAILED, message, errors);
    }

    public ValidationException(FieldErrorDetail singleError) {
        this("We found problems that need your attention.", List.of(singleError));
    }
}

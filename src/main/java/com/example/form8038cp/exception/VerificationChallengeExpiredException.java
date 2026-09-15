package com.example.form8038cp.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class VerificationChallengeExpiredException extends ApiException {
    public VerificationChallengeExpiredException(String message) {
        super(HttpStatus.GONE, ApiErrorCode.GONE, message, List.of());
    }
}

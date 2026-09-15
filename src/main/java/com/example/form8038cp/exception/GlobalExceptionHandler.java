package com.example.form8038cp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ErrorResponse.of(ex.getCode(), ex.getMessage(), ex.getErrors()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> FieldErrorDetail.of(
                        fe.getCode() != null ? fe.getCode().toUpperCase() : "INVALID",
                        fe.getField(),
                        fe.getDefaultMessage()))
                .toList();
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(ApiErrorCode.VALIDATION_FAILED,
                        "We found problems that need your attention.", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ApiErrorCode.SERVER_ERROR,
                        "An unexpected error occurred.", List.of()));
    }
}

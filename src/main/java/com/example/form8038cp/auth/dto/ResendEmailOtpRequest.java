package com.example.form8038cp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendEmailOtpRequest {

    @NotBlank(message = "Challenge ID is required")
    private String challengeId;
}

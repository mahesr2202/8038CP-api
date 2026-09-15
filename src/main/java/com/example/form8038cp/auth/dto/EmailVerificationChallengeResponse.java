package com.example.form8038cp.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@RequiredArgsConstructor
public class EmailVerificationChallengeResponse implements LoginOutcome {
    private final String challengeId;
    private final String maskedEmail;
    private final OffsetDateTime expiresAt;
}

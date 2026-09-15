package com.example.form8038cp.auth.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Sealed union returned by POST /auth/login.
 * Carries either a JWT (LoginResponse) when the account is verified,
 * or a verification challenge (EmailVerificationChallengeResponse) when it is not.
 * The client distinguishes the two by checking for the presence of "token".
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(LoginResponse.class),
    @JsonSubTypes.Type(EmailVerificationChallengeResponse.class)
})
public interface LoginOutcome {
}

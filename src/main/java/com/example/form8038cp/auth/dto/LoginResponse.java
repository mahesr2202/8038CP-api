package com.example.form8038cp.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginResponse implements LoginOutcome {
    private final UserResponse user;
    private final String token;
    private final long expiresAt;
}

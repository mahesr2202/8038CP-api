package com.example.form8038cp.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RefreshResponse {
    private final String token;
    private final long expiresInSeconds;
}

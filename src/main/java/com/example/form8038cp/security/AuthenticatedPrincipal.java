package com.example.form8038cp.security;

import java.time.Instant;

/**
 * JWT-derived principal stored in the SecurityContext for an authenticated request.
 * issuedAt is carried so JwtAuthenticationFilter can compare it against
 * tokens_valid_from and reject tokens minted before the last credential change.
 */
public record AuthenticatedPrincipal(Long userId, String email, String fullName, String role, Instant issuedAt) {
}

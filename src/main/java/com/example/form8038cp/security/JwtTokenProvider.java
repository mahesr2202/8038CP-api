package com.example.form8038cp.security;

import com.example.form8038cp.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_USER_ID  = "userId";
    private static final String CLAIM_FULL_NAME = "name";
    private static final String CLAIM_ROLE      = "role";
    private static final String CLAIM_CHANNEL   = "channel";

    private static final int MIN_SECRET_BYTES = 32;

    private final Key key;
    private final String issuer;
    private final long defaultExpirationMs;
    private final long rememberMeExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-ms}") long defaultExpirationMs,
            @Value("${app.jwt.remember-me-expiration-ms}") long rememberMeExpirationMs) {
        this.key = Keys.hmacShaKeyFor(requireStrongSecret(secret));
        this.issuer = issuer;
        this.defaultExpirationMs = defaultExpirationMs;
        this.rememberMeExpirationMs = rememberMeExpirationMs;
    }

    private static byte[] requireStrongSecret(String secret) {
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes; was "
                    + bytes.length + ". Set APP_JWT_SECRET to a long random value.");
        }
        return bytes;
    }

    public String generateToken(User user, boolean rememberMe) {
        Date now = new Date();
        long ttl = rememberMe ? rememberMeExpirationMs : defaultExpirationMs;
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(user.getEmail())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_FULL_NAME, user.getFullName())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_CHANNEL, user.getChannel().name())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ttl))
                .signWith(key)
                .compact();
    }

    public long getExpirationSeconds(boolean rememberMe) {
        return (rememberMe ? rememberMeExpirationMs : defaultExpirationMs) / 1000;
    }

    public Optional<AuthenticatedPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Long userId  = claims.get(CLAIM_USER_ID, Long.class);
            String email = claims.getSubject();
            String name  = claims.get(CLAIM_FULL_NAME, String.class);
            String role  = claims.get(CLAIM_ROLE, String.class);
            if (userId == null || email == null) return Optional.empty();
            Date iat = claims.getIssuedAt();
            return Optional.of(new AuthenticatedPrincipal(
                    userId, email, name, role, iat == null ? null : iat.toInstant()));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected JWT: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}

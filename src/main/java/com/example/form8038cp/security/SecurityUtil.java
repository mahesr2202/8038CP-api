package com.example.form8038cp.security;

import com.example.form8038cp.exception.UnauthenticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static AuthenticatedPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
            throw new UnauthenticatedException("Authentication is required to access this resource.");
        }
        return principal;
    }

    public static Long currentUserId() {
        return currentPrincipal().userId();
    }
}

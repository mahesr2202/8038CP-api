package com.example.form8038cp.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the real client IP, honouring the CF-Connecting-IP header set by Cloudflare
 * so that rate limiting keys on the filer's address, not the edge node's.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {}

    public static String resolve(HttpServletRequest request) {
        String cfIp = request.getHeader("CF-Connecting-IP");
        if (cfIp != null && !cfIp.isBlank()) return cfIp.trim();
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}

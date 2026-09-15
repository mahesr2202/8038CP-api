package com.example.form8038cp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory, per-IP fixed-window rate limiter for the unauthenticated auth endpoints.
 * Single-node only — state lives in this JVM, revisit if the app scales horizontally.
 *
 * <p>Keys on {@link ClientIpResolver} (not raw remoteAddr) so that requests arriving
 * through Cloudflare are keyed on the filer's real IP, not the edge node.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS   = 60_000L;
    private static final int  MAX_TRACKED = 50_000;
    private static final int  DEFAULT_MAX = 10;
    private static final int  RESEND_MAX  = 3;

    private static final Map<String, Integer> LIMITS = Map.of(
            "/api/v1/auth/signup",              DEFAULT_MAX,
            "/api/v1/auth/login",               DEFAULT_MAX,
            "/api/v1/auth/verify-email",        DEFAULT_MAX,
            "/api/v1/auth/verify-email/resend", RESEND_MAX,
            "/api/v1/auth/forgot-password",     DEFAULT_MAX,
            "/api/v1/auth/reset-password",      DEFAULT_MAX);

    private final Map<String, Window> buckets = new ConcurrentHashMap<>();

    private static final class Window {
        long start;
        int  count;
        Window(long start) { this.start = start; this.count = 1; }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        Integer max = LIMITS.get(req.getRequestURI());
        if (max == null) {
            chain.doFilter(req, res);
            return;
        }
        long now = System.currentTimeMillis();
        String key = ClientIpResolver.resolve(req) + "|" + req.getRequestURI();
        if (!allow(key, now, max)) {
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write(
                    "{\"error\":\"RATE_LIMITED\","
                    + "\"message\":\"Too many attempts. Please wait a minute and try again.\"}");
            return;
        }
        chain.doFilter(req, res);
    }

    private boolean allow(String key, long now, int max) {
        if (buckets.size() > MAX_TRACKED) {
            buckets.entrySet().removeIf(e -> now - e.getValue().start >= WINDOW_MS);
        }
        Window w = buckets.compute(key, (k, cur) -> {
            if (cur == null || now - cur.start >= WINDOW_MS) return new Window(now);
            cur.count++;
            return cur;
        });
        return w.count <= max;
    }
}

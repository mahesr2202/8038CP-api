package com.example.form8038cp.security;

import com.example.form8038cp.auth.entity.User;
import com.example.form8038cp.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            jwtTokenProvider.parse(token).ifPresent(this::authenticateIfUserStillActive);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticateIfUserStillActive(AuthenticatedPrincipal principal) {
        Optional<User> optUser = userRepository.findById(principal.userId());
        if (optUser.isEmpty()) return;
        User user = optUser.get();
        if (!user.isActive() || !user.getEmail().equalsIgnoreCase(principal.email())) return;

        // Reject tokens minted before the last password reset (tokens_valid_from cutoff).
        var cutoff = user.getTokensValidFrom();
        if (cutoff != null && (principal.issuedAt() == null
                || principal.issuedAt().isBefore(cutoff.toInstant()))) {
            return;
        }

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().toUpperCase()));
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

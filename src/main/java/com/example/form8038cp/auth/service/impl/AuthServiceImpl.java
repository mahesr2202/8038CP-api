package com.example.form8038cp.auth.service.impl;

import com.example.form8038cp.auth.dto.ChangePasswordRequest;
import com.example.form8038cp.auth.dto.EmailVerificationChallengeResponse;
import com.example.form8038cp.auth.dto.LoginOutcome;
import com.example.form8038cp.auth.dto.LoginRequest;
import com.example.form8038cp.auth.dto.LoginResponse;
import com.example.form8038cp.auth.dto.RefreshResponse;
import com.example.form8038cp.auth.dto.SignupRequest;
import com.example.form8038cp.auth.entity.EmailVerificationChallenge;
import com.example.form8038cp.auth.entity.PasswordResetToken;
import com.example.form8038cp.auth.entity.User;
import com.example.form8038cp.auth.mapper.UserMapper;
import com.example.form8038cp.auth.repository.EmailVerificationChallengeRepository;
import com.example.form8038cp.auth.repository.PasswordResetTokenRepository;
import com.example.form8038cp.auth.repository.UserRepository;
import com.example.form8038cp.auth.service.AuthService;
import com.example.form8038cp.email.EmailService;
import com.example.form8038cp.exception.ConflictException;
import com.example.form8038cp.exception.FieldErrorDetail;
import com.example.form8038cp.exception.ResetTokenGoneException;
import com.example.form8038cp.exception.ResourceNotFoundException;
import com.example.form8038cp.exception.UnauthenticatedException;
import com.example.form8038cp.exception.ValidationException;
import com.example.form8038cp.exception.VerificationChallengeExpiredException;
import com.example.form8038cp.security.JwtTokenProvider;
import com.example.form8038cp.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String RESET_LINK_EXPIRED_MESSAGE =
            "This reset link has expired or has already been used.";
    private static final String VERIFICATION_EXPIRED_MESSAGE =
            "This verification code has expired or was already used. Please sign in again for a new one.";
    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationChallengeRepository emailVerificationChallengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Value("${app.password-reset.token-expiry-minutes}")
    private long resetTokenExpiryMinutes;

    @Value("${app.frontend.reset-password-url}")
    private String frontendResetPasswordUrl;

    @Value("${app.email-otp.code-expiry-minutes}")
    private long emailOtpExpiryMinutes;

    @Value("${app.email-otp.max-attempts}")
    private int emailOtpMaxAttempts;

    @Override
    @Transactional
    public EmailVerificationChallengeResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists.");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .channel(request.getChannel() != null ? request.getChannel() : User.Channel.WEB)
                .role(User.Role.USER)
                .status(User.Status.ACTIVE)
                .emailVerified(false)
                .termsAcceptedAt(OffsetDateTime.now())
                .build();
        user = userRepository.save(user);

        return issueEmailVerificationChallenge(user);
    }

    @Override
    @Transactional
    public LoginOutcome login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (AuthenticationException ex) {
            throw new UnauthenticatedException("Invalid credentials.");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthenticatedException("Invalid credentials."));

        if (!user.isEmailVerified()) {
            return issueEmailVerificationChallenge(user);
        }

        String token = jwtTokenProvider.generateToken(user, request.isRememberMe());
        long expiresAt = System.currentTimeMillis()
                + jwtTokenProvider.getExpirationSeconds(request.isRememberMe()) * 1000L;
        return new LoginResponse(userMapper.toResponse(user), token, expiresAt);
    }

    @Override
    @Transactional
    public void verifyEmailOtp(String challengeId, String code) {
        EmailVerificationChallenge challenge = emailVerificationChallengeRepository
                .findByChallengeId(challengeId)
                .orElseThrow(() -> new VerificationChallengeExpiredException(VERIFICATION_EXPIRED_MESSAGE));

        if (challenge.isUsed() || challenge.isExpired()) {
            throw new VerificationChallengeExpiredException(VERIFICATION_EXPIRED_MESSAGE);
        }

        if (challenge.getAttempts() >= emailOtpMaxAttempts) {
            challenge.setUsed(true);
            emailVerificationChallengeRepository.save(challenge);
            throw new VerificationChallengeExpiredException(
                    "Too many incorrect attempts. Please request a new code.");
        }

        if (!challenge.getCode().equals(code)) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            emailVerificationChallengeRepository.save(challenge);
            throw new ValidationException(
                    FieldErrorDetail.of("INVALID_CODE", "code", "That code doesn't match. Try again."));
        }

        challenge.setUsed(true);
        emailVerificationChallengeRepository.save(challenge);

        User user = challenge.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        emailService.sendWelcome(user.getEmail(),
                user.getFullName() != null ? user.getFullName() : user.getEmail());
    }

    @Override
    @Transactional
    public EmailVerificationChallengeResponse resendEmailOtp(String challengeId) {
        EmailVerificationChallenge challenge = emailVerificationChallengeRepository
                .findByChallengeId(challengeId)
                .orElseThrow(() -> new VerificationChallengeExpiredException(VERIFICATION_EXPIRED_MESSAGE));

        if (challenge.isUsed()) {
            throw new VerificationChallengeExpiredException(VERIFICATION_EXPIRED_MESSAGE);
        }

        challenge.setCode(generateOtpCode());
        challenge.setExpiresAt(OffsetDateTime.now().plusMinutes(emailOtpExpiryMinutes));
        challenge.setAttempts(0);
        challenge = emailVerificationChallengeRepository.save(challenge);

        emailService.sendEmailVerificationOtp(challenge.getUser().getEmail(), challenge.getCode());
        return new EmailVerificationChallengeResponse(
                challenge.getChallengeId(),
                maskEmail(challenge.getUser().getEmail()),
                challenge.getExpiresAt());
    }

    @Override
    public void logout() {
        SecurityUtil.currentUserId();
    }

    @Override
    public RefreshResponse refresh() {
        Long userId = SecurityUtil.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthenticatedException(
                        "Authentication is required to access this resource."));
        String token = jwtTokenProvider.generateToken(user, false);
        return new RefreshResponse(token, jwtTokenProvider.getExpirationSeconds(false));
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmailIgnoreCase(normalizeEmail(email)).ifPresent(user -> {
            String token = UUID.randomUUID().toString().replace("-", "");
            PasswordResetToken reset = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(OffsetDateTime.now().plusMinutes(resetTokenExpiryMinutes))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(reset);
            String resetLink = frontendResetPasswordUrl + "?token=" + token;
            emailService.sendPasswordReset(user.getEmail(), resetLink);
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken reset = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException(
                        "We found problems that need your attention.",
                        List.of(FieldErrorDetail.of("INVALID_TOKEN", "token", RESET_LINK_EXPIRED_MESSAGE))));

        if (reset.isUsed() || reset.isExpired()) {
            throw new ResetTokenGoneException(RESET_LINK_EXPIRED_MESSAGE);
        }

        User user = reset.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setTokensValidFrom(OffsetDateTime.now());
        userRepository.save(user);

        reset.setUsed(true);
        passwordResetTokenRepository.save(reset);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("We found problems that need your attention.",
                    List.of(FieldErrorDetail.of("INCORRECT_PASSWORD", "currentPassword",
                            "Your current password is incorrect.")));
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ValidationException("We found problems that need your attention.",
                    List.of(FieldErrorDetail.of("SAME_AS_CURRENT", "newPassword",
                            "New password must be different from your current password.")));
        }

        user.setTokensValidFrom(OffsetDateTime.now());
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private EmailVerificationChallengeResponse issueEmailVerificationChallenge(User user) {
        String code = generateOtpCode();
        EmailVerificationChallenge challenge = EmailVerificationChallenge.builder()
                .user(user)
                .challengeId(UUID.randomUUID().toString())
                .code(code)
                .expiresAt(OffsetDateTime.now().plusMinutes(emailOtpExpiryMinutes))
                .build();
        challenge = emailVerificationChallengeRepository.save(challenge);
        emailService.sendEmailVerificationOtp(user.getEmail(), code);
        return new EmailVerificationChallengeResponse(
                challenge.getChallengeId(), maskEmail(user.getEmail()), challenge.getExpiresAt());
    }

    private static String generateOtpCode() {
        return String.format("%06d", OTP_RANDOM.nextInt(1_000_000));
    }

    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        String local = email.substring(0, at);
        String domain = email.substring(at);
        String masked = local.length() <= 2
                ? local.charAt(0) + "***"
                : local.charAt(0) + "***" + local.charAt(local.length() - 1);
        return masked + domain;
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}

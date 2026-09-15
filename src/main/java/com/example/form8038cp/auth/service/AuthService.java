package com.example.form8038cp.auth.service;

import com.example.form8038cp.auth.dto.ChangePasswordRequest;
import com.example.form8038cp.auth.dto.EmailVerificationChallengeResponse;
import com.example.form8038cp.auth.dto.LoginOutcome;
import com.example.form8038cp.auth.dto.LoginRequest;
import com.example.form8038cp.auth.dto.RefreshResponse;
import com.example.form8038cp.auth.dto.SignupRequest;

public interface AuthService {

    EmailVerificationChallengeResponse signup(SignupRequest request);

    LoginOutcome login(LoginRequest request);

    void verifyEmailOtp(String challengeId, String code);

    EmailVerificationChallengeResponse resendEmailOtp(String challengeId);

    void logout();

    RefreshResponse refresh();

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    void changePassword(Long userId, ChangePasswordRequest request);
}

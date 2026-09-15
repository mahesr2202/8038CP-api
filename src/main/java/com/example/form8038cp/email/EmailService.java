package com.example.form8038cp.email;

public interface EmailService {
    void sendEmailVerificationOtp(String toEmail, String code);
    void sendWelcome(String toEmail, String name);
    void sendPasswordReset(String toEmail, String resetLink);
}

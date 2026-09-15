package com.example.form8038cp.email.impl;

import com.example.form8038cp.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendEmailVerificationOtp(String toEmail, String code) {
        send(toEmail,
                "Your Form 8038-CP verification code",
                "Your verification code is: " + code + "\n\nThis code expires in 15 minutes.\n\n" +
                "If you did not request this, you can safely ignore this email.");
    }

    @Override
    public void sendWelcome(String toEmail, String name) {
        send(toEmail,
                "Welcome to Form 8038-CP",
                "Hi " + name + ",\n\n" +
                "Your email address has been verified. You can now sign in and file your Form 8038-CP.\n\n" +
                "Thank you for using our service.");
    }

    @Override
    public void sendPasswordReset(String toEmail, String resetLink) {
        send(toEmail,
                "Reset your Form 8038-CP password",
                "Click the link below to reset your password:\n\n" + resetLink +
                "\n\nThis link expires in 60 minutes.\n\n" +
                "If you did not request a password reset, you can safely ignore this email.");
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}

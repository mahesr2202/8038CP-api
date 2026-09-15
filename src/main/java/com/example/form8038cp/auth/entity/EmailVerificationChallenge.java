package com.example.form8038cp.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "email_verification_challenges",
    schema = "form8038cp",
    indexes = @Index(name = "idx_evc_challenge_id", columnList = "challenge_id", unique = true)
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "challenge_id", nullable = false, unique = true, length = 36)
    private String challengeId;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    @Builder.Default
    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }
}

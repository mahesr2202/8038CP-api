package com.example.form8038cp.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Application user account. The `channel` column differentiates the originating
 * integration (web portal, mobile app, direct API, partner system, etc.) so that
 * analytics, rate limits, and UX flows can be scoped per channel without separate
 * user tables.
 */
@Entity
@Table(name = "users", schema = "form8038cp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    public enum Role { USER, ADMIN }

    public enum Status { ACTIVE, INACTIVE, SUSPENDED }

    /**
     * Identifies the integration channel through which this account was created.
     * Each channel may have its own rate limits, branding, and feature flags.
     *
     * <ul>
     *   <li>WEB     – browser-based portal (default for self-service filers)</li>
     *   <li>MOBILE  – iOS/Android native app</li>
     *   <li>API     – direct machine-to-machine API key clients</li>
     *   <li>PARTNER – white-label or reseller integrations</li>
     * </ul>
     */
    public enum Channel { WEB, MOBILE, API, PARTNER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 75)
    private String email;

    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String passwordHash;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.ACTIVE;

    /**
     * Originating channel. Set once at account creation and never changed — if a user
     * later accesses via a different surface, a separate account (or channel-scoped
     * session) is expected. Stored as a VARCHAR so adding a new enum value is a Flyway
     * migration, not a code-and-data migration.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Channel channel = Channel.WEB;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /**
     * Timestamp of ToS/Privacy Policy consent captured at signup.
     * Nullable so rows predating this column keep working under ddl-auto=validate.
     */
    @Column(name = "terms_accepted_at")
    private OffsetDateTime termsAcceptedAt;

    /**
     * Tokens issued before this instant are rejected by JwtAuthenticationFilter.
     * Stamped on password reset so existing sessions cannot outlive a credential change.
     * NULL means no cutoff.
     */
    @Column(name = "tokens_valid_from")
    private OffsetDateTime tokensValidFrom;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.role == null) this.role = Role.USER;
        if (this.status == null) this.status = Status.ACTIVE;
        if (this.channel == null) this.channel = Channel.WEB;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isActive() {
        return Status.ACTIVE == status;
    }
}

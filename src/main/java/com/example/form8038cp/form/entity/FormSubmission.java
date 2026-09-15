package com.example.form8038cp.form.entity;

import com.example.form8038cp.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "form_submissions", schema = "form8038cp")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormSubmission {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Column(name = "is_amended", nullable = false)
    private Boolean isAmended = false;

    @Column(name = "stripe_payment_intent_id", length = 50) private String stripePaymentIntentId;
    @Column(name = "payment_status", length = 20)           private String paymentStatus;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20) private String status = "DRAFT";

    @Column(name = "submitted_at")                                      private OffsetDateTime submittedAt;
    @Column(name = "created_at", nullable = false, updatable = false)   private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)                      private OffsetDateTime updatedAt;

    /** IRS e-file XML generated at submission time, validated against the 2027v1.0 XSD. */
    @Column(name = "generated_xml", columnDefinition = "TEXT")
    private String generatedXml;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}

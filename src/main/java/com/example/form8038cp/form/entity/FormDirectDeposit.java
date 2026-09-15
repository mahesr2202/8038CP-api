package com.example.form8038cp.form.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "form_direct_deposit", schema = "form8038cp")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormDirectDeposit {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private FormSubmission submission;

    @Column(name = "routing_number", length = 9)  private String routingNumber;
    @Column(name = "account_type", length = 10)   private String accountType;
    @Column(name = "account_number", length = 17) private String accountNumber;
}

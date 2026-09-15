package com.example.form8038cp.form.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "form_signature", schema = "form8038cp")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormSignature {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private FormSubmission submission;

    @Column(name = "sig_signature", length = 100) private String sigSignature;
    @Column(name = "sig_date", length = 10)       private String sigDate;
    @Column(name = "sig_name_title", length = 80) private String sigNameTitle;
    @Column(name = "taxpayer_pin", length = 5)    private String taxpayerPin;
    @Column(name = "sig_first_name", length = 35) private String sigFirstName;
    @Column(name = "sig_last_name", length = 35)  private String sigLastName;
}

package com.example.form8038cp.form.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "form_paid_preparer", schema = "form8038cp")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormPaidPreparer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private FormSubmission submission;

    @Column(name = "prep_name", length = 60)         private String prepName;
    @Column(name = "prep_signature", length = 60)    private String prepSignature;
    @Column(name = "prep_date", length = 10)         private String prepDate;
    @Column(name = "prep_self_employed")              private Boolean prepSelfEmployed;
    @Column(name = "prep_ptin", length = 11)         private String prepPtin;
    @Column(name = "prep_firm_name", length = 60)    private String prepFirmName;
    @Column(name = "prep_firm_ein", length = 10)     private String prepFirmEin;
    @Column(name = "prep_phone", length = 20)        private String prepPhone;
    @Column(name = "prep_firm_address", length = 80) private String prepFirmAddress;
}

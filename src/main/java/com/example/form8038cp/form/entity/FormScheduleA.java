package com.example.form8038cp.form.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "form_schedule_a", schema = "form8038cp")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormScheduleA {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private FormSubmission submission;

    @Column(name = "row_order", nullable = false)
    private int rowOrder;

    @Column(name = "col_a_maturity_date", length = 10)
    private String colAMaturityDate;

    @Column(name = "col_b_actual_interest", precision = 15, scale = 2)
    private BigDecimal colBActualInterest;

    @Column(name = "col_c_credit_rate_interest", precision = 15, scale = 2)
    private BigDecimal colCCreditRateInterest;
}

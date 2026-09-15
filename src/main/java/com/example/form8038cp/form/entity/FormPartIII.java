package com.example.form8038cp.form.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "form_part_iii", schema = "form8038cp")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormPartIII {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private FormSubmission submission;

    @Column(name = "line18", length = 10)         private String line18;
    @Column(name = "line19a", length = 20)        private String line19a;
    @Column(name = "line19b", length = 10)        private String line19b;
    @Column(name = "line19c", length = 20)        private String line19c;
    @Column(name = "line20_type", length = 3)     private String line20Type;
    @Column(name = "line21a", length = 20)        private String line21a;
    @Column(name = "line21b", length = 20)        private String line21b;
    @Column(name = "line21c_code", length = 5)    private String line21cCode;
    @Column(name = "line21c_date", length = 10)   private String line21cDate;
    @Column(name = "line22", precision = 15, scale = 2) private BigDecimal line22;
    @Column(name = "line23a", length = 5)         private String line23a;
    @Column(name = "line23b", length = 3)         private String line23b;
    @Column(name = "line24a", length = 5)         private String line24a;
    @Column(name = "line24b", length = 5)         private String line24b;
    @Column(name = "line25", length = 5)          private String line25;
}

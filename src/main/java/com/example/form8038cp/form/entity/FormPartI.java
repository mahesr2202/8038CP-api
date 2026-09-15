package com.example.form8038cp.form.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "form_part_i", schema = "form8038cp")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormPartI {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private FormSubmission submission;

    @Column(name = "line1", length = 80)         private String line1;
    @Column(name = "line2", length = 10)          private String line2;
    @Column(name = "line3_street", length = 60)   private String line3Street;
    @Column(name = "line3_room", length = 10)     private String line3Room;
    @Column(name = "line4", length = 60)          private String line4;
    @Column(name = "line5", length = 60)          private String line5;
    @Column(name = "line5_title", length = 35)    private String line5Title;
    @Column(name = "line6", length = 20)          private String line6;
}

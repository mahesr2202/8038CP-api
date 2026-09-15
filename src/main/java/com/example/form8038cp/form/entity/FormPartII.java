package com.example.form8038cp.form.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "form_part_ii", schema = "form8038cp")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormPartII {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private FormSubmission submission;

    @Column(name = "line7", length = 80)          private String line7;
    @Column(name = "line8", length = 10)          private String line8;
    @Column(name = "line9_street", length = 60)   private String line9Street;
    @Column(name = "line9_room", length = 10)     private String line9Room;
    @Column(name = "line10", length = 3)          private String line10;
    @Column(name = "line11", length = 60)         private String line11;
    @Column(name = "line12", length = 10)         private String line12;
    @Column(name = "line13", length = 80)         private String line13;
    @Column(name = "line14", length = 10)         private String line14;
    @Column(name = "line15", length = 60)         private String line15;
    @Column(name = "line15_title", length = 35)   private String line15Title;
    @Column(name = "line16", length = 20)         private String line16;
    @Column(name = "line17a", length = 10)        private String line17a;
    @Column(name = "line17b", length = 20)        private String line17b;
    @Column(name = "line17c", length = 5)         private String line17c;
}

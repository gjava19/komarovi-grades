package com.example.komarovi.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "total_score",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_upload_student_subject",
                        columnNames = {"upload_id","student_id","subject_name","subject_code"}
                )
        },
        indexes = {
                @Index(name="idx_total_upload", columnList = "upload_id"),
                @Index(name="idx_total_student", columnList = "student_id"),
                @Index(name="idx_total_teacher", columnList = "subject_teacher_id")
        }
)
public class TotalScore  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="upload_id", nullable = false)
    private Upload upload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="subject_teacher_id")
    private Teacher subjectTeacher;

    @Column(name="subject_name", nullable = false, length = 50)
    private String subjectName;

    @Column(name="subject_code", length = 50)
    private String subjectCode;

    @Column(name="total_points", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalPoints;

    @OneToMany(mappedBy = "totalScore", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskScore> tasks = new ArrayList<>();

}

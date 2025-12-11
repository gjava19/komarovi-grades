package com.example.komarovi.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="class_group")
public class ClassGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="grade", nullable = false)
    private Integer grade;

    @Column(name="parallel", nullable = false)
    private Integer parallel;

    @Column(name="class_code", nullable = false, unique = true, length = 10)
    private String classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="homeroom_teacher_id")
    private Teacher homeroomTeacher;

    @OneToMany(mappedBy = "classGroup")
    private List<Student> students = new ArrayList<>();

}

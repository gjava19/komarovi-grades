package com.example.komarovi.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "teacher")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="teacher_code", nullable = false, unique = true, length = 20)
    private String teacherCode;

    @Column(name="full_name", nullable = false, length = 150)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false, length = 1)
    private TeacherStatus status;

}

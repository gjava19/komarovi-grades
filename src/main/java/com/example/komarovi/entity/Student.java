package com.example.komarovi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        name = "student",
        indexes = {@Index(name="idx_student_class", columnList = "class_group_id")}
)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // უნიკალური კოდი (მაგ. 12345)
    @Column(name="student_code", nullable = false, unique = true, length = 20)
    private String studentCode;

//    // აქ ინახება BCrypt-ით დაშიფრული პაროლი
//    @Column(nullable = false)
//    private String passwordHash;


    @Column(name="last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name="first_name", nullable = false, length = 100)
    private String firstName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="class_group_id", nullable = false)
    private ClassGroup classGroup;
}

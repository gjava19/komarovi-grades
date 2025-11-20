package com.example.komarovi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // უნიკალური კოდი (მაგ. 12345)
    @Column(unique = true, nullable = false)
    private String code;

    // აქ ინახება BCrypt-ით დაშიფრული პაროლი
    @Column(nullable = false)
    private String passwordHash;

}

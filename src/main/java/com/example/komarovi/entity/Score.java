package com.example.komarovi.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "score")
@Getter
@Setter
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    private String subject;        // საგანი (მაგ: მათემატიკა)
    private String subjectCode;    // საგნის კოდი (მაგ: M049)
    private Double totalScore;     // ჯამური ქულა (24.0)

    private String taskName;       // ამოცანა 1, ამოცანა 2, ...
    private Double score;          // ამოცანის ქულა

}

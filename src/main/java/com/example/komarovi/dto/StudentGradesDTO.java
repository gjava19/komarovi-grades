package com.example.komarovi.dto;


import java.util.Map;

public class StudentGradesDTO {
    public String studentCode;
    public String subject;
    public String subjectCode;
    public Double totalScore;
    public Map<String, Double> tasks;
}
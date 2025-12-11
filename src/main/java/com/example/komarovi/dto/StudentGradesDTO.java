package com.example.komarovi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class StudentGradesDTO {
    public String studentCode;

    public Integer assessmentNo;
    public LocalDateTime uploadedAt;
    public String fileName;

    public String subjectName;
    public String subjectCode; // შენთან “კოდი” (ადგილი/ვარიანტი)

    public BigDecimal totalPoints;
    public Map<Integer, BigDecimal> tasks; // taskNo -> points
}
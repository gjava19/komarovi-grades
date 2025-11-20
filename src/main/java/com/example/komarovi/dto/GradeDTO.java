package com.example.komarovi.dto;

import java.util.List;

public record GradeDTO(
        String studentCode,
        String subject,
        String subjectCode,
        Double totalScore,
        List<ScoreDTO> scores
) {}
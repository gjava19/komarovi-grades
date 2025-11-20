package com.example.komarovi.controller;

import com.example.komarovi.dto.StudentGradesDTO;
import com.example.komarovi.entity.Score;
import com.example.komarovi.entity.Student;
import com.example.komarovi.repository.ScoreRepository;
import com.example.komarovi.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class GradeController {

    private final StudentRepository studentRepo;
    private final ScoreRepository scoreRepo;

    public GradeController(StudentRepository studentRepo, ScoreRepository scoreRepo) {
        this.studentRepo = studentRepo;
        this.scoreRepo = scoreRepo;
    }

    @GetMapping("/grades")
    public List<StudentGradesDTO> getGrades(@RequestParam String password) {

        // მოძებნა კოდით (პაროლი == მოსწავლის კოდი)
        Student st = studentRepo.findByCode(password).orElse(null);
        if (st == null) return List.of();

        List<Score> scores = scoreRepo.findByStudent(st);

        // ჯგუფირება საგნის კოდით
        Map<String, List<Score>> grouped = scores.stream()
                .collect(Collectors.groupingBy(Score::getSubjectCode));

        List<StudentGradesDTO> result = new ArrayList<>();

        for (var entry : grouped.entrySet()) {

            String subjectCode = entry.getKey();
            List<Score> subjectScores = entry.getValue();

            StudentGradesDTO dto = new StudentGradesDTO();

            dto.studentCode = st.getCode();
            dto.subjectCode = subjectCode;
            dto.subject = subjectScores.get(0).getSubject();
            dto.totalScore = subjectScores.get(0).getTotalScore(); // Excel totalScore

            Map<String, Double> tasks = new LinkedHashMap<>();
            for (Score sc : subjectScores) {
                tasks.put(sc.getTaskName(), sc.getScore());
            }
            dto.tasks = tasks;

            result.add(dto);
        }

        return result;
    }

}


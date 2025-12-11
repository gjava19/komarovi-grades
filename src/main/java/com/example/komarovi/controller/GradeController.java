package com.example.komarovi.controller;

import org.springframework.web.bind.annotation.*;
import com.example.komarovi.dto.StudentGradesDTO;
import com.example.komarovi.entity.Student;
import com.example.komarovi.entity.TotalScore;
import com.example.komarovi.repository.StudentRepository;
import com.example.komarovi.repository.TotalScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GradeController {
    private final StudentRepository studentRepo;
    private final TotalScoreRepository totalScoreRepo;

    /**
     * მოსწავლე ხედავს თავის ქულებს.
     * GET /api/grades?studentCode=KL96&password=KL96
     * optional: &assessmentNo=3
     */
    @GetMapping("/grades")
    public List<StudentGradesDTO> getGrades(
            @RequestParam String studentCode,
          //  @RequestParam String password,
            @RequestParam(required = false) Integer assessmentNo
    ) {
        Student st = studentRepo.findByStudentCode(studentCode).orElse(null);
        if (st == null) return List.of();

//        // BCrypt შედარება
//        if (!BCrypt.checkpw(password, st.getPasswordHash())) {
//            return List.of();
//        }

        // ყველა TotalScore ამ მოსწავლისთვის (ყველა upload/exam)
        List<TotalScore> totals = totalScoreRepo.findAllByStudentIdOrderByUpload_UploadedAtDesc(st.getId());

        // თუ assessmentNo მიუთითა — გაფილტრე
        if (assessmentNo != null) {
            totals = totals.stream()
                    .filter(ts -> ts.getUpload() != null && Objects.equals(ts.getUpload().getAssessmentNo(), assessmentNo))
                    .toList();
        }

        // DTO-ებად გარდაქმნა
        return totals.stream().map(ts -> {
            StudentGradesDTO dto = new StudentGradesDTO();

            dto.studentCode = st.getStudentCode();

            dto.assessmentNo = ts.getUpload() != null ? ts.getUpload().getAssessmentNo() : null;
            dto.uploadedAt   = ts.getUpload() != null ? ts.getUpload().getUploadedAt() : null;
            dto.fileName     = ts.getUpload() != null ? ts.getUpload().getOriginalName() : null;

            dto.subjectName = ts.getSubjectName();
            dto.subjectCode = ts.getSubjectCode();
            dto.totalPoints = ts.getTotalPoints();

            // tasks -> Map<taskNo, points> (ნომრით დალაგებული)
            dto.tasks = ts.getTasks().stream()
                    .sorted(Comparator.comparingInt(t -> t.getTaskNumber() == null ? 0 : t.getTaskNumber()))
                    .collect(Collectors.toMap(
                            t -> t.getTaskNumber(),
                            t -> t.getTaskPoints(),
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));

            return dto;
        }).toList();
    }

}


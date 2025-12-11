package com.example.komarovi.controller;

import com.example.komarovi.dto.TeacherViewDTO;
import com.example.komarovi.entity.TotalScore;
import com.example.komarovi.repository.ClassGroupRepository;
import com.example.komarovi.repository.TeacherRepository;
import com.example.komarovi.repository.TotalScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeacherSearchController {

    private final TeacherRepository teacherRepo;
    private final ClassGroupRepository classGroupRepo;
    private final TotalScoreRepository totalScoreRepo;

    // 1) საგნის მასწავლებლის კოდით + კლასის კოდით
    // GET /api/teacher/grades?teacherCode=55BK&classCode=81&assessmentNo=3
    @GetMapping("/teacher/grades")
    public List<TeacherViewDTO> bySubjectTeacherAndClass(
            @RequestParam String teacherCode,
            @RequestParam String classCode,
            @RequestParam(required = false) Integer assessmentNo
    ) {
        var teacher = teacherRepo.findByTeacherCode(teacherCode).orElse(null);
        if (teacher == null) return List.of();

        // კლასის არსებობაც შევამოწმოთ (optional)
        if (classGroupRepo.findByClassCode(classCode).isEmpty()) return List.of();

        return totalScoreRepo.findForSubjectTeacherInClass(teacher.getId(), classCode, assessmentNo)
                .stream().map(this::toDTO).toList();
    }

    // 2) დამრიგებლის კოდით + კლასის კოდით
    // GET /api/homeroom/grades?homeroomTeacherCode=22EN&classCode=81&assessmentNo=3
    @GetMapping("/homeroom/grades")
    public List<TeacherViewDTO> byHomeroomTeacherAndClass(
            @RequestParam String homeroomTeacherCode,
            @RequestParam String classCode,
            @RequestParam(required = false) Integer assessmentNo
    ) {
        var homeroom = teacherRepo.findByTeacherCode(homeroomTeacherCode).orElse(null);
        if (homeroom == null) return List.of();

        if (classGroupRepo.findByClassCode(classCode).isEmpty()) return List.of();

        return totalScoreRepo.findForHomeroomTeacherInClass(homeroom.getId(), classCode, assessmentNo)
                .stream().map(this::toDTO).toList();
    }

    private TeacherViewDTO toDTO(TotalScore ts) {
        TeacherViewDTO dto = new TeacherViewDTO();
        dto.classCode = ts.getStudent().getClassGroup().getClassCode();

        dto.studentCode = ts.getStudent().getStudentCode();
        dto.lastName = ts.getStudent().getLastName();
        dto.firstName = ts.getStudent().getFirstName();

        dto.assessmentNo = ts.getUpload().getAssessmentNo();
        dto.uploadedAt = ts.getUpload().getUploadedAt();

        dto.subjectName = ts.getSubjectName();
        dto.subjectCode = ts.getSubjectCode();

        dto.totalPoints = ts.getTotalPoints();

        dto.tasks = ts.getTasks().stream()
                .sorted(Comparator.comparingInt(t -> t.getTaskNumber() == null ? 0 : t.getTaskNumber()))
                .collect(Collectors.toMap(
                        t -> t.getTaskNumber(),
                        t -> t.getTaskPoints(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        return dto;
    }
}


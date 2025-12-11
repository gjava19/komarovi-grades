package com.example.komarovi.dto;

import com.example.komarovi.entity.TotalScore;

public class TeacherViewDTO {

    public String classCode;

    public String studentCode;
    public String lastName;
    public String firstName;

    public Integer assessmentNo;
    public java.time.LocalDateTime uploadedAt;

    public String subjectName;
    public String subjectCode;

    public java.math.BigDecimal totalPoints;
    public java.util.Map<Integer, java.math.BigDecimal> tasks;

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
                .sorted(java.util.Comparator.comparingInt(t -> t.getTaskNumber() == null ? 0 : t.getTaskNumber()))
                .collect(java.util.stream.Collectors.toMap(
                        t -> t.getTaskNumber(),
                        t -> t.getTaskPoints(),
                        (a, b) -> a,
                        java.util.LinkedHashMap::new
                ));

        return dto;
    }

}

package com.example.komarovi.dto;

public class StudentDTO {
    public Long id;
    public String studentCode;
    public String lastName;
    public String firstName;
    public String classCode; // 81

    public static StudentDTO from(com.example.komarovi.entity.Student s) {
        StudentDTO d = new StudentDTO();
        d.id = s.getId();
        d.studentCode = s.getStudentCode();
        d.lastName = s.getLastName();
        d.firstName = s.getFirstName();
        d.classCode = (s.getClassGroup() != null) ? s.getClassGroup().getClassCode() : null;
        return d;
    }
}

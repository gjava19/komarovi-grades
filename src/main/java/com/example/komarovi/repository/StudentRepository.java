package com.example.komarovi.repository;


import com.example.komarovi.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode); // ან findByCode თუ ეგ გაქვს entity-ში
    List<Student> findAllByClassGroupId(Long classGroupId);
    boolean existsByStudentCode(String studentCode);
}


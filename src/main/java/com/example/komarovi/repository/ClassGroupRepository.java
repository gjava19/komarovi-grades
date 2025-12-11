package com.example.komarovi.repository;

import com.example.komarovi.entity.ClassGroup;
import com.example.komarovi.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, Long> {
    Optional<ClassGroup> findByClassCode(String classCode);
}


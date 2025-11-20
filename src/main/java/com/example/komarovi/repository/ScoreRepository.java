package com.example.komarovi.repository;

import com.example.komarovi.entity.Score;
import com.example.komarovi.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByStudent(Student student);
}


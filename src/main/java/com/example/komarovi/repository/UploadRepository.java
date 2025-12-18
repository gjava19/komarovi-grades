package com.example.komarovi.repository;

import com.example.komarovi.entity.Upload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface UploadRepository extends JpaRepository<Upload, Long> {
    @Transactional
    void deleteAllByAssessmentNo(Integer assessmentNo);

    @Transactional
    void deleteAllByAssessmentNoAndUploadedAtBetween(
            Integer assessmentNo,
            LocalDateTime from,
            LocalDateTime to
    );
    List<Upload> findAllByAssessmentNoOrderByUploadedAtDesc(Integer assessmentNo);

    @Transactional
    long deleteByAssessmentNoAndUploadedAtGreaterThanEqualAndUploadedAtLessThan(
            Integer assessmentNo,
            LocalDateTime from,
            LocalDateTime to
    );

    List<Upload> findAllByOrderByUploadedAtDesc();

    List<Upload> findAllByAssessmentNoAndUploadedAtGreaterThanEqualAndUploadedAtLessThanOrderByUploadedAtDesc(
            Integer assessmentNo, LocalDateTime from, LocalDateTime to
    );
    boolean existsByAssessmentNo(Integer assessmentNo);

    @Query("select max(u.assessmentNo) from Upload u")
    Integer findMaxAssessmentNo();

    List<Upload> findAllByAssessmentNo(Integer assessmentNo);

}

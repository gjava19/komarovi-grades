package com.example.komarovi.repository;

import com.example.komarovi.entity.TotalScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TotalScoreRepository extends JpaRepository<TotalScore, Long> {
    Optional<TotalScore> findByUploadIdAndStudentIdAndSubjectNameAndSubjectCode(
            Long uploadId, Long studentId, String subjectName, String subjectCode
    );
    List<TotalScore> findAllByStudentIdOrderByUpload_UploadedAtDesc(Long studentId);

    // საგნის მასწავლებელი + კლასი (+ სურვილისამებრ assessmentNo)
    @Query("""
        select ts
        from TotalScore ts
        join fetch ts.student s
        join fetch s.classGroup cg
        join fetch ts.upload u
        left join fetch ts.tasks t
        where ts.subjectTeacher.id = :teacherId
          and cg.classCode = :classCode
          and (:assessmentNo is null or u.assessmentNo = :assessmentNo)
        order by s.lastName, s.firstName, ts.subjectName, u.uploadedAt desc
    """)
    List<TotalScore> findForSubjectTeacherInClass(
            @Param("teacherId") Long teacherId,
            @Param("classCode") String classCode,
            @Param("assessmentNo") Integer assessmentNo
    );

    // დამრიგებელი + კლასი (+ სურვილისამებრ assessmentNo) => კლასის ყველა საგნის ქულა
    @Query("""
        select ts
        from TotalScore ts
        join fetch ts.student s
        join fetch s.classGroup cg
        join fetch ts.upload u
        left join fetch ts.tasks t
        where cg.homeroomTeacher.id = :homeroomTeacherId
          and cg.classCode = :classCode
          and (:assessmentNo is null or u.assessmentNo = :assessmentNo)
        order by s.lastName, s.firstName, ts.subjectName, u.uploadedAt desc
    """)
    List<TotalScore> findForHomeroomTeacherInClass(
            @Param("homeroomTeacherId") Long homeroomTeacherId,
            @Param("classCode") String classCode,
            @Param("assessmentNo") Integer assessmentNo
    );
}

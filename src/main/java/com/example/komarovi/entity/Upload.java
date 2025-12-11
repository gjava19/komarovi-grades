package com.example.komarovi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "uploads",
        indexes = {
                @Index(name = "idx_uploads_uploaded_at", columnList = "uploaded_at"),
                @Index(name = "idx_uploads_assessment_no", columnList = "assessment_no")
        }
)
public class Upload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="uploaded_at", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name="file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name="original_name", length = 255)
    private String originalName;

    @Column(name="assessment_no", nullable = false)
    private Integer assessmentNo;

    @OneToMany(mappedBy = "upload", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TotalScore> totalScores = new ArrayList<>();

}

package com.example.komarovi.dto;

import java.time.LocalDateTime;

public class UploadDTO {
    public Long id;
    public Integer assessmentNo;
    public LocalDateTime uploadedAt;
    public String originalName;
    public String fileName;

    public static UploadDTO from(com.example.komarovi.entity.Upload u) {
        UploadDTO d = new UploadDTO();
        d.id = u.getId();
        d.assessmentNo = u.getAssessmentNo();
        d.uploadedAt = u.getUploadedAt();
        d.originalName = u.getOriginalName();
        d.fileName = u.getFileName();
        return d;
    }
}

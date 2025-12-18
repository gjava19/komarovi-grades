package com.example.komarovi.services;

import com.example.komarovi.entity.Upload;
import com.example.komarovi.repository.UploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UploadAdminService {
    private final UploadRepository uploadRepo;

    @Transactional
    public void deleteByAssessmentNo(int assessmentNo) {
        List<Upload> uploads = uploadRepo.findAllByAssessmentNo(assessmentNo);
        uploadRepo.deleteAll(uploads);
    }

    @Transactional
    public long deleteByAssessmentNoAndTimeRange(int assessmentNo, LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from და to აუცილებელია");
        }
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("from უნდა იყოს to-ზე ნაკლები");
        }
        return uploadRepo.deleteByAssessmentNoAndUploadedAtGreaterThanEqualAndUploadedAtLessThan(assessmentNo, from, to);
    }
    @Transactional(readOnly = true)
    public List<Upload> listAll() {
        return uploadRepo.findAllByOrderByUploadedAtDesc();
    }
    @Transactional(readOnly = true)
    public List<Upload> listByAssessmentNo(int assessmentNo) {
        return uploadRepo.findAllByAssessmentNoOrderByUploadedAtDesc(assessmentNo);
    }

    @Transactional(readOnly = true)
    public List<Upload> listByAssessmentNoAndTimeRange(int assessmentNo, LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) throw new IllegalArgumentException("from და to აუცილებელია");
        if (!from.isBefore(to)) throw new IllegalArgumentException("from უნდა იყოს to-ზე ნაკლები");
        return uploadRepo.findAllByAssessmentNoAndUploadedAtGreaterThanEqualAndUploadedAtLessThanOrderByUploadedAtDesc(
                assessmentNo, from, to
        );
    }
}

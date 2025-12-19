package com.example.komarovi.controller;

import com.example.komarovi.dto.UploadDTO;
import com.example.komarovi.services.UploadAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Delete-Admin - Uploads", description = "Upload - ების მართვა (წაშლა assessmentNo თი)")
public class UploadAdminController {

    private final UploadAdminService uploadAdminService;

    @DeleteMapping("/uploads/by-assessment")
    @Operation(
            summary = "წაშლის upload-ებს assessmentNo-ით",
            description = "შლის uploads ჩანაწერებს და CASCADE-ით წაიშლება total_score და tasks_score."
    )
    public Map<String, Object> deleteByAssessmentNo(
            @RequestParam
            @Parameter(example = "9") int assessmentNo
    ) {
         uploadAdminService.deleteByAssessmentNo(assessmentNo);
        return Map.of(
                "assessmentNo", assessmentNo
        );
    }


    @GetMapping("/uploads")
    @Operation(
            summary = "ნახე uploads (ფილტრით assessmentNo)",
            description = "თუ assessmentNo არ მიუთითე — ყველა upload ჩამოიტანს."
    )
    public List<UploadDTO> listUploads(
            @RequestParam(required = false)
            @Parameter() Integer assessmentNo
//,
//            @RequestParam(required = false)
//            @DateTimeFormat(iso = ISO.DATE_TIME)
//            @Parameter(example = "2025-12-01T00:00:00") LocalDateTime from,
//
//            @RequestParam(required = false)
//            @DateTimeFormat(iso = ISO.DATE_TIME)
//            @Parameter(example = "2025-12-08T00:00:00") LocalDateTime to
    ) {
        if (assessmentNo == null) {
            return uploadAdminService.listAll().stream().map(UploadDTO::from).toList();
        }

//        if (from != null || to != null) {
//            if (from == null || to == null) {
//                throw new IllegalArgumentException("თუ დროით ფილტრავ, ორივე from და to უნდა მიუთითო");
//            }
//            return uploadAdminService.listByAssessmentNoAndTimeRange(assessmentNo, from, to)
//                    .stream().map(UploadDTO::from).toList();
//        }

        return uploadAdminService.listByAssessmentNo(assessmentNo).stream().map(UploadDTO::from).toList();
    }


//    @DeleteMapping("/uploads")
//    @Operation(
//            summary = "წაშლის upload-ებს assessmentNo + uploaded_at დიაპაზონით",
//            description = "დროის ფანჯარა არის [from, to). CASCADE წაშლის ქულებსაც."
//    )
//    public Map<String, Object> deleteByAssessmentNoAndTimeRange(
//            @RequestParam @Parameter(example = "3") int assessmentNo,
//            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) @Parameter(example = "2025-12-01T00:00:00") LocalDateTime from,
//            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) @Parameter(example = "2025-12-08T00:00:00") LocalDateTime to
//    ) {
//        long deleted = uploadAdminService.deleteByAssessmentNoAndTimeRange(assessmentNo, from, to);
//        return Map.of(
//                "assessmentNo", assessmentNo,
//                "from", from.toString(),
//                "to", to.toString(),
//                "deletedUploads", deleted
//        );
//    }
    @Operation(summary = "ყველა ცხრილის ერთიანად გასუფთავება", description = "შლის ყველა ცხრილის ყველა ჩანაწერს")
    @DeleteMapping("/clear-db")
    public ResponseEntity<?> clearDb() {
        uploadAdminService.clearAllTables();
        return ResponseEntity.ok("OK: all tables cleared");
    }
}

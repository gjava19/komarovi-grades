package com.example.komarovi.controller;

import com.example.komarovi.dto.StudentCodeUpdateRequest;
import com.example.komarovi.dto.StudentDTO;
import com.example.komarovi.services.StudentAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
@Tag(name = "Admin - Students", description = "მოსწავლის მოძებნა და studentCode-ის შეცვლა")
public class StudentAdminController {

    private final StudentAdminService studentAdminService;

    // GET /api/admin/students/by-code?code=KL96
    @GetMapping("/by-code")
    @Operation(summary = "მოსწავლის დაბრუნება studentCode-ით")
    public StudentDTO getStudentByCode(@RequestParam String code) {
        var st = studentAdminService.getByCode(code);
        return (st == null) ? null : StudentDTO.from(st);
    }

    // PUT /api/admin/students/code  body: { "oldCode": "KL96", "newCode": "KL97" }
    @PutMapping("/code")
    @Operation(summary = "მოსწავლის კოდის შეცვლა (oldCode -> newCode)")
    public StudentDTO updateStudentCode(@Valid @RequestBody StudentCodeUpdateRequest req) {
        var updated = studentAdminService.updateStudentCode(req.oldCode, req.newCode);
        return StudentDTO.from(updated);
    }
}

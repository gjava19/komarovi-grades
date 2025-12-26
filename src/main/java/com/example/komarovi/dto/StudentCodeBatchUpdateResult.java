package com.example.komarovi.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class StudentCodeBatchUpdateResult {

    private int totalRows;
    private int updatedCount;
    private int failedCount;

    private List<RowError> errors = new ArrayList<>();

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class RowError {
        private int row;          // Excel row index (1-based readable)
        private String oldCode;
        private String newCode;
        private String reason;
    }

    public void addError(int row, String oldCode, String newCode, String reason) {
        errors.add(new RowError(row, oldCode, newCode, reason));
        failedCount = errors.size();
    }
}

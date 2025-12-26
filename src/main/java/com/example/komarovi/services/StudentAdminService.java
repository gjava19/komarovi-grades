package com.example.komarovi.services;

import com.example.komarovi.dto.StudentCodeBatchUpdateResult;
import com.example.komarovi.entity.Student;
import com.example.komarovi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentAdminService {

    private final StudentRepository studentRepo;
    private static final int MAX_ROWS = 5000;

    @Transactional(readOnly = true)
    public Student getByCode(String code) {
        return studentRepo.findByStudentCode(code).orElse(null);
    }

    @Transactional
    public Student updateStudentCode(String oldCode, String newCode) {
        final String oldC = oldCode.trim();
        final String newC = newCode.trim();

        Student st = studentRepo.findByStudentCode(oldC)
                .orElseThrow(() -> new IllegalArgumentException("Old code ვერ მოიძებნა: " + oldC));

        if (studentRepo.existsByStudentCode(newC)) {
            throw new IllegalArgumentException("New code უკვე არსებობს: " + newC);
        }

        st.setStudentCode(newC);

        // თუ შენთან "password = studentCode" პოლიტიკაა, სჯობს პაროლიც განაახლო:
        // st.setPasswordHash(BCrypt.hashpw(newCode, BCrypt.gensalt()));

        return studentRepo.save(st);
    }

    @Transactional
    public StudentCodeBatchUpdateResult updateStudentCodesFromExcel(MultipartFile file) {
        var result = new StudentCodeBatchUpdateResult();
        if (file == null || file.isEmpty()) {
            result.addError(0, null, null, "ფაილი ცარიელია");
            return result;
        }
        String name = (file.getOriginalFilename() == null) ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".xlsx")) {
            result.addError(0, null, null, "ფაილი უნდა იყოს .xlsx");
            return result;
        }

        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) {
                result.addError(0, null, null, "Sheet ვერ მოიძებნა");
                return result;
            }

            DataFormatter fmt = new DataFormatter();

            // --- header map (პირველი რიგი) ---
            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) {
                result.addError(0, null, null, "Header row ვერ მოიძებნა");
                return result;
            }

            Map<String, Integer> col = headerColumns(header, fmt);

            Integer oldIdx = firstMatch(col,
                    "oldcode", "old_code", "old code", "ძველი", "ძველი კოდი", "ძველი_code", "ძველიcode");
            Integer newIdx = firstMatch(col,
                    "newcode", "new_code", "new code", "ახალი", "ახალი კოდი", "ახალი_code", "ახალicode");

            if (oldIdx == null || newIdx == null) {
                result.addError(1, null, null,
                        "Header-ში საჭიროა სვეტები: oldCode და newCode (ან ქართული სათაურები: ძველი კოდი / ახალი კოდი)");
                return result;
            }

            int firstDataRow = header.getRowNum() + 1;
            int lastRow = sheet.getLastRowNum();

            int rowsProcessed = 0;
            Set<String> newCodesInFile = new HashSet<>();

            for (int r = firstDataRow; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String oldCode = clean(fmt.formatCellValue(row.getCell(oldIdx)));
                String newCode = clean(fmt.formatCellValue(row.getCell(newIdx)));

                // ცარიელი რიგები გამოტოვე
                if (oldCode.isBlank() && newCode.isBlank()) continue;

                rowsProcessed++;
                if (rowsProcessed > MAX_ROWS) {
                    result.addError(r + 1, oldCode, newCode, "რიგების ლიმიტი გადაცდა (MAX_ROWS=" + MAX_ROWS + ")");
                    break;
                }

                // ვალიდაციები
                if (oldCode.isBlank() || newCode.isBlank()) {
                    result.addError(r + 1, oldCode, newCode, "oldCode/newCode ცარიელია");
                    continue;
                }
                if (oldCode.equalsIgnoreCase(newCode)) {
                    result.addError(r + 1, oldCode, newCode, "oldCode და newCode ერთია");
                    continue;
                }
                if (!newCodesInFile.add(newCode.toUpperCase())) {
                    result.addError(r + 1, oldCode, newCode, "ფაილში newCode დუბლიკატია");
                    continue;
                }

                // DB ოპერაცია
                Student st = studentRepo.findByStudentCodeIgnoreCase(oldCode).orElse(null);
                if (st == null) {
                    result.addError(r + 1, oldCode, newCode, "მოსწავლე ვერ მოიძებნა oldCode-ით");
                    continue;
                }

                boolean newExists = studentRepo.existsByStudentCodeIgnoreCase(newCode);
                if (newExists) {
                    result.addError(r + 1, oldCode, newCode, "newCode უკვე არსებობს ბაზაში");
                    continue;
                }

                st.setStudentCode(newCode);
                // save არაა აუცილებელი თუ entity managed-ია, მაგრამ აჯობებს გასაგებად:
                studentRepo.save(st);

                result.setUpdatedCount(result.getUpdatedCount() + 1);
            }

            result.setTotalRows(rowsProcessed);
            result.setFailedCount(result.getErrors().size());
            return result;

        } catch (Exception e) {
            result.addError(0, null, null, "Excel-ის დამუშავების შეცდომა: " + e.getMessage());
            return result;
        }
    }

    private static Map<String, Integer> headerColumns(Row header, DataFormatter fmt) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell c : header) {
            String key = normalizeHeader(fmt.formatCellValue(c));
            if (!key.isBlank()) map.put(key, c.getColumnIndex());
        }
        return map;
    }

    private static Integer firstMatch(Map<String, Integer> col, String... candidates) {
        for (String s : candidates) {
            Integer idx = col.get(normalizeHeader(s));
            if (idx != null) return idx;
        }
        return null;
    }

    private static String normalizeHeader(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase()
                .replace("\uFEFF", "")          // BOM
                .replaceAll("\\s+", " ");       // multiple spaces
    }

    private static String clean(String s) {
        return (s == null) ? "" : s.trim();
    }
}

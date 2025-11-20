package com.example.komarovi.services;

import com.example.komarovi.entity.Score;
import com.example.komarovi.entity.Student;
import com.example.komarovi.repository.ScoreRepository;
import com.example.komarovi.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

@Service
public class ExcelLoader {

    private final StudentRepository studentRepo;
    private final ScoreRepository scoreRepo;

    public ExcelLoader(StudentRepository studentRepo, ScoreRepository scoreRepo) {
        this.studentRepo = studentRepo;
        this.scoreRepo = scoreRepo;
    }

    @Transactional
    public void loadExcel(String path) throws Exception {
        try (InputStream fis = new FileInputStream(path);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            if (!rows.hasNext()) return;

            rows.next(); // skip header row
            System.out.println("Loading Excel...");
            while (rows.hasNext()) {
                Row r = rows.next();
                if (r == null) continue;

                String code = getCellString(r.getCell(0));       // მოსწავლის კოდი
                String subject = getCellString(r.getCell(1));    // საგანი
                String subjectCode = getCellString(r.getCell(2));// საგნის კოდი
                Double total = getCellDouble(r.getCell(3));       // ჯამური ქულა

                if (code == null || code.isEmpty()) continue;


                // Student lookup or create
                Student st = studentRepo.findByCode(code).orElse(null);

                if (st == null) {
                    st = new Student();
                    st.setCode(code);
                    st.setPasswordHash(BCrypt.hashpw(code, BCrypt.gensalt()));
                    studentRepo.save(st);
                }

                // 7 task scores
                String[] taskNames = {
                        "ამოცანა 1", "ამოცანა 2", "ამოცანა 3",
                        "ამოცანა 4", "ამოცანა 5", "ამოცანა 6", "ამოცანა 7"
                };
              //  System.out.println("Student: " + code + " Subject: " + subject);
                for (int i = 0; i < 7; i++) {
                    Double sc = getCellDouble(r.getCell(4 + i));
                    if (sc == null) continue;

                    Score s = new Score();
                    s.setStudent(st);        // მოსწავლე (Foreign Key)
                    s.setSubject(subject);   // საგანი
                    s.setSubjectCode(subjectCode); // საგნის კოდი
                    s.setTotalScore(total);  // ჯამური ქულა
                    s.setTaskName(taskNames[i]); // ამოცანის სახელი
                    s.setScore(sc);          // ამოცანის ქულა

                    scoreRepo.save(s);


                }

            }
            System.out.println("ulalaaa");
        }
    }

    private Double getCellDouble(Cell c) {
        if (c == null) return null;

        if (c.getCellType() == CellType.NUMERIC) {
            return c.getNumericCellValue();
        }
        if (c.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(c.getStringCellValue());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }


    private String getCellString(Cell c) {
        if (c == null) return null;
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue();
            case NUMERIC -> String.valueOf(c.getNumericCellValue());
            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
            default -> null;
        };
    }
}
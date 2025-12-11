package com.example.komarovi.services;

import com.example.komarovi.entity.*;
import com.example.komarovi.repository.*;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.*;
@Service
@AllArgsConstructor
public class ExcelLoader {

    private final StudentRepository studentRepo;
    private final UploadRepository uploadRepo;
    private final TeacherRepository teacherRepo;
    private final ClassGroupRepository classGroupRepo;
    private final TotalScoreRepository totalScoreRepo;

    /** მთავარი მეთოდი — Swagger upload-იდანაც ეს უნდა გამოიძახო */
    @Transactional
    public void loadExcel(InputStream in, String originalName, int assessmentNo) throws Exception {
        // WorkbookFactory უკეთესია .xlsx/.xlsm/.xls-სთვის

//        if(uploadRepo.existsByAssessmentNo(assessmentNo) ) {
//            throw new IllegalArgumentException("assessmentNo is exists");
//        }
        // cache-ები რომ DB-ს არ ურტყა ყოველ რიგზე
        Map<String, Teacher> teacherCache = new HashMap<>();
        Map<String, ClassGroup> classCache = new HashMap<>();
        Map<String, Student> studentCache = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);  //პირველი შიტი
            Iterator<Row> rows = sheet.iterator(); // დგას 0 ოვანზე ჩვენი შიტის
            if (!rows.hasNext()) return;

            DataFormatter fmt = new DataFormatter();
            Row headerRow  = rows.next();
            if (headerRow == null) return;
            short last = headerRow .getLastCellNum();
            if (last < 0) last = 0;

            int studentCodeCol = -1, nameCol=-1, lastNameCol=-1, classCol=-1, parallelCol=-1, classCodeCol=-1;
            int homeroomTeacherNameCol=-1, homeroomTeacherCodeCol=-1;
            int subjectTeacherNameCol=-1, subjectTeacherCodeCol=-1;
            int subjectCol=-1, seatCodeCol=-1, totalScoreCol=-1,statusCol=-1;

            Map<Integer, Integer> taskToCol = new TreeMap<>();   // 1..30 sorted
            Pattern taskPat = Pattern.compile("^ამოცანა\\s*(\\d+)$"); // "ამოცანა 1" ... "ამოცანა 30"

            for (int c = 0; c < last; c++) {
                String text = fmt.formatCellValue(headerRow.getCell(c)).trim();


                if (text.equals("მოსწ კოდი")) studentCodeCol = c;
                else if (text.equals("სახელი")) nameCol = c;
                else if (text.equals("გვარი")) lastNameCol = c;
                else if (text.equals("კლასი")) classCol = c;
                else if (text.equals("პარალელი")) parallelCol = c;
                else if (text.equals("კლასები")) classCodeCol = c;
                else if (text.equals("დამრიგებელი")) homeroomTeacherNameCol = c;
                else if (text.equals("დამრიგებ. კოდი")) homeroomTeacherCodeCol = c;
                else if (text.equals("საგნის მასწავლებელი")) subjectTeacherNameCol = c;
                else if (text.equals("საგნის მასწ. კოდი")) subjectTeacherCodeCol = c;
                else if (text.equals("საგანი")) subjectCol = c;
                else if (text.equals("კოდი")) seatCodeCol = c;
                else if (text.equals("ჯამი")||text.equals("ქულა"))  totalScoreCol = c;
                else if (text.equals("სტატუსი")) statusCol = c;

                Matcher m = taskPat.matcher(text);
                if (m.matches()) taskToCol.put(Integer.parseInt(m.group(1)), c);
            }

            if (studentCodeCol == -1)throw new IllegalStateException("\"მოსწ კოდი\" ვერ ვიპოვე header-ში");
            if (totalScoreCol == -1) throw new IllegalStateException("\"ჯამი/ქულა\" ვერ ვიპოვე header-ში");

            // 1) Create Upload row (ბაჩი)
            Upload upload = new Upload();
            upload.setOriginalName(originalName);
            upload.setFileName(sheet.getSheetName());
            upload.setAssessmentNo(assessmentNo);
            upload = uploadRepo.save(upload);

            while (rows.hasNext()) {
                Row r = rows.next();
                if (r == null) continue;

                String studentCode = getCellString(r.getCell(studentCodeCol));
                if (studentCode == null || studentCode.isBlank()) continue;

                String firstName = (nameCol>=0) ? getCellString(r.getCell(nameCol)) : null;
                String lastName  = (lastNameCol>=0) ? getCellString(r.getCell(lastNameCol)) : null;

                Integer grade    = (classCol>=0) ? toInt(getCellDouble(r.getCell(classCol))) : null;
                Integer parallel = (parallelCol>=0) ? toInt(getCellDouble(r.getCell(parallelCol))) : null;

                String classCode = (classCodeCol>=0) ? getCellString(r.getCell(classCodeCol)) : null; // "81"
                String homeroomTeacherCode = (homeroomTeacherCodeCol>=0) ? getCellString(r.getCell(homeroomTeacherCodeCol)) : null;
                String homeroomTeacherName = (homeroomTeacherNameCol>=0) ? getCellString(r.getCell(homeroomTeacherNameCol)) : null;

                String subjectTeacherCode = (subjectTeacherCodeCol>=0) ? getCellString(r.getCell(subjectTeacherCodeCol)) : null;
                String subjectTeacherName = (subjectTeacherNameCol>=0) ? getCellString(r.getCell(subjectTeacherNameCol)) : null;

                String subjectName = (subjectCol>=0) ? getCellString(r.getCell(subjectCol)) : null;  // "მათემატიკა"
                String seatCode    = (seatCodeCol>=0) ? getCellString(r.getCell(seatCodeCol)) : null; // შენთან "კოდი" (ადგილი/ვარიანტი)
                Double total       = getCellDouble(r.getCell(totalScoreCol));
//              if (subjectName == null || subjectName.isBlank() || total == null) continue;

                // 2) Teachers (find or create)
                Teacher homeroomTeacher = upsertTeacher(teacherCache, homeroomTeacherCode, homeroomTeacherName, TeacherStatus.D);
                Teacher subjectTeacher  = upsertTeacher(teacherCache, subjectTeacherCode,  subjectTeacherName,  TeacherStatus.T);

                // 3) ClassGroup (find or create)
                ClassGroup cg = upsertClassGroup(classCache, classCode, grade, parallel, homeroomTeacher);

                // 4) Student (find or create/update)
                Student st = studentCache.computeIfAbsent(studentCode, code ->
                        studentRepo.findByStudentCode(code).orElse(null)
                );
                if (st == null) {
                    st = new Student();
                    st.setStudentCode(studentCode);
                    st.setFirstName(firstName);
                    st.setLastName(lastName);
                    st.setClassGroup(cg);
                 //   st.setPasswordHash(BCrypt.hashpw(studentCode, BCrypt.gensalt()));
                    st = studentRepo.save(st);
                    studentCache.put(studentCode, st);
                } else {
                    boolean changed = false;
                    if (firstName != null && !firstName.equals(st.getFirstName())) { st.setFirstName(firstName); changed = true; }
                    if (lastName != null && !lastName.equals(st.getLastName())) { st.setLastName(lastName); changed = true; }
                    if (cg != null && (st.getClassGroup()==null || !cg.getId().equals(st.getClassGroup().getId()))) { st.setClassGroup(cg); changed = true; }
                    if (changed) st = studentRepo.save(st);
                }
                if (subjectName == null || subjectName.isBlank() || total == null) continue;

                // 5) TotalScore (upsert in same upload)
                TotalScore ts = totalScoreRepo
                        .findByUploadIdAndStudentIdAndSubjectNameAndSubjectCode(upload.getId(), st.getId(), subjectName, seatCode)
                        .orElseGet(TotalScore::new);

                ts.setUpload(upload);
                ts.setStudent(st);
                ts.setSubjectTeacher(subjectTeacher);
                ts.setSubjectName(subjectName);
                ts.setSubjectCode(seatCode);
                ts.setTotalPoints(BigDecimal.valueOf(total));

                // 6) Task scores
                ts.getTasks().clear(); // თუ იგივე upload-ში იგივე ჩანაწერს ხელახლა ვწერთ, ამოცანები არ დაგიდუბლირდეს

                for (Map.Entry<Integer,Integer> e : taskToCol.entrySet()) {
                    Integer taskNo = e.getKey();
                    Double pts = getCellDouble(r.getCell(e.getValue()));
                    if (pts == null) continue;

                    TaskScore t = new TaskScore();
                    t.setTotalScore(ts);
                    t.setTaskNumber(taskNo);
                    t.setTaskPoints(BigDecimal.valueOf(pts));
                    ts.getTasks().add(t);
                }

                // save (თუ TotalScore-ში tasks-ზე CascadeType.ALL გაქვს, ერთ save-ზე ყველაფერი წავა)
                totalScoreRepo.save(ts);
            }
        }
    }
    private Teacher upsertTeacher(Map<String, Teacher> cache, String code, String name, TeacherStatus status) {
        if (code == null || code.isBlank()) return null;

        return cache.computeIfAbsent(code, c -> {
            Teacher t = teacherRepo.findByTeacherCode(c).orElseGet(Teacher::new);
            t.setTeacherCode(c);
            if (name != null && !name.isBlank()) t.setFullName(name);
            t.setStatus(status);
            return teacherRepo.save(t);
        });
    }

    private ClassGroup upsertClassGroup(Map<String, ClassGroup> cache, String classCode, Integer grade, Integer parallel, Teacher homeroomTeacher) {
        if (classCode == null || classCode.isBlank()) return null;

        return cache.computeIfAbsent(classCode, cc -> {
            ClassGroup cg = classGroupRepo.findByClassCode(cc).orElseGet(ClassGroup::new);
            cg.setClassCode(cc);
            if (grade != null) cg.setGrade(grade);
            if (parallel != null) cg.setParallel(parallel);
            if (homeroomTeacher != null) cg.setHomeroomTeacher(homeroomTeacher);
            return classGroupRepo.save(cg);
        });
    }

    private Integer toInt(Double d) {
        return (d == null) ? null : (int)Math.round(d);
    }

    private Double getCellDouble(Cell c) {
        if (c == null) return null;

        return switch (c.getCellType()) {
            case NUMERIC -> c.getNumericCellValue();
            case STRING -> {
                String v = c.getStringCellValue();
                if (v == null) yield null;
                v = v.trim().replace(",", "."); // თუ ვინმემ 1,5 დაწერა
                try { yield Double.parseDouble(v); }
                catch (Exception e) { yield null; }
            }
            case FORMULA -> {
                try { yield c.getNumericCellValue(); }
                catch (Exception e) { yield null; }
            }
            default -> null;
        };
    }

    private String getCellString(Cell c) {
        if (c == null) return null;

        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue().trim();
            case NUMERIC -> {
                // რომ 1234.0 არ გამოვიდეს მოსწავლის კოდზე
                double n = c.getNumericCellValue();
                long asLong = (long) n;
                yield (n == asLong) ? String.valueOf(asLong) : String.valueOf(n);
            }
            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
            case FORMULA -> {
                // ფორმულა თუ ტექსტს აბრუნებს
                try { yield c.getStringCellValue().trim(); }
                catch (Exception e) {
                    try {
                        double n = c.getNumericCellValue();
                        long asLong = (long) n;
                        yield (n == asLong) ? String.valueOf(asLong) : String.valueOf(n);
                    } catch (Exception ex) { yield null; }
                }
            }
            default -> null;
        };
    }
}
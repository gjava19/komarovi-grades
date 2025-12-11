package com.example.komarovi.services;

import com.example.komarovi.entity.Student;
import com.example.komarovi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentAdminService {

    private final StudentRepository studentRepo;

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
}

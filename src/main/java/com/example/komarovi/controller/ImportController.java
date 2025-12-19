package com.example.komarovi.controller;

import com.example.komarovi.services.ExcelLoader;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@AllArgsConstructor
public class ImportController {

    private final ExcelLoader loader;

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Excel ფაილის ატვირთვა",
            description = """
    იღებს .xlsx Excel ფაილს, კითხულობს მონაცემებს (მოსწავლეები/ქულები/ატვირთვის ინფორმაცია)
    და ინახავს ბაზაში. წარმატების შემთხვევაში აბრუნებს ატვირთვის code 200.
    """
    )
    public Map<String, Object> upload(@RequestPart("file") MultipartFile file, int assessmentNo) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try (InputStream in = file.getInputStream()) {
            loader.loadExcel(in, file.getOriginalFilename(),assessmentNo);
        }
        return Map.of(
                "status", "ok",
                "fileName", file.getOriginalFilename(),
                "size", file.getSize()
        );
    }
}


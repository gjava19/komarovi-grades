package com.example.komarovi.dto;

import jakarta.validation.constraints.NotBlank;

public class StudentCodeUpdateRequest {
    @NotBlank public String oldCode;
    @NotBlank public String newCode;
}

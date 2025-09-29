package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BlogRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Long authorId
) {}

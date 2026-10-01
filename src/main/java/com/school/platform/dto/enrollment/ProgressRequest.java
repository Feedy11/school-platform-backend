package com.school.platform.dto.enrollment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProgressRequest(
        @NotNull(message = "La progression est obligatoire")
        @Min(value = 0, message = "La progression doit être >= 0")
        @Max(value = 100, message = "La progression doit être <= 100")
        Integer progress
) {}
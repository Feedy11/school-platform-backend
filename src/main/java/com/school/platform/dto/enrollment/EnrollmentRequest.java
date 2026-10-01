package com.school.platform.dto.enrollment;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
        @NotNull(message = "courseId est obligatoire")
        Long courseId
) {}
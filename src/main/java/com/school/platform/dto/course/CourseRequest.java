package com.school.platform.dto.course;

import com.school.platform.entity.Level;
import jakarta.validation.constraints.*;

public record CourseRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 150, message = "Le titre ne doit pas dépasser 150 caractères")
        String title,

        @NotBlank(message = "La description est obligatoire")
        String description,

        @NotBlank(message = "La catégorie est obligatoire")
        @Size(max = 100, message = "La catégorie ne doit pas dépasser 100 caractères")
        String category,

        @NotNull(message = "Le niveau est obligatoire")
        Level level,

        @NotNull(message = "La durée est obligatoire")
        @Positive(message = "La durée doit être positive")
        Integer duration,

        @Size(max = 255, message = "L'URL de l'image ne doit pas dépasser 255 caractères")
        String imageUrl
) {}
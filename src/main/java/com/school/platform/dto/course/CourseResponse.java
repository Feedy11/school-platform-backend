package com.school.platform.dto.course;

import com.school.platform.entity.Course;
import com.school.platform.entity.Level;

import java.time.LocalDateTime;

public record CourseResponse(
        Long id,
        String title,
        String description,
        String category,
        Level level,
        Integer duration,
        String imageUrl,
        LocalDateTime createdAt
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCategory(),
                course.getLevel(),
                course.getDuration(),
                course.getImageUrl(),
                course.getCreatedAt());
    }
}
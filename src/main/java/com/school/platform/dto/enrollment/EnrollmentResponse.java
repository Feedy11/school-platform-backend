package com.school.platform.dto.enrollment;

import com.school.platform.dto.course.CourseResponse;
import com.school.platform.entity.Enrollment;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        Long enrollmentId,
        Long courseId,
        String title,
        CourseResponse course,
        int progress,
        String status,
        LocalDateTime enrolledAt
) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                CourseResponse.from(enrollment.getCourse()),
                enrollment.getProgress(),
                enrollment.getStatus() != null ? enrollment.getStatus().name() : "APPROVED",
                enrollment.getEnrolledAt());
    }
}
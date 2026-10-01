package com.school.platform.dto.admin;

import com.school.platform.dto.course.CourseResponse;
import com.school.platform.entity.Enrollment;

import java.time.LocalDateTime;

public record AdminEnrollmentResponse(
        Long id,
        Long enrollmentId,
        Long userId,
        String studentName,
        String studentEmail,
        Long courseId,
        String courseTitle,
        CourseResponse course,
        int progress,
        String status,
        LocalDateTime enrolledAt,
        LocalDateTime reviewedAt,
        String rejectionReason
) {
    public static AdminEnrollmentResponse from(Enrollment enrollment) {
        return new AdminEnrollmentResponse(
                enrollment.getId(),
                enrollment.getId(),
                enrollment.getUser().getId(),
                enrollment.getUser().getFirstName() + " " + enrollment.getUser().getLastName(),
                enrollment.getUser().getEmail(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                CourseResponse.from(enrollment.getCourse()),
                enrollment.getProgress(),
                enrollment.getStatus() != null ? enrollment.getStatus().name() : "APPROVED",
                enrollment.getEnrolledAt(),
                enrollment.getReviewedAt(),
                enrollment.getRejectionReason());
    }
}
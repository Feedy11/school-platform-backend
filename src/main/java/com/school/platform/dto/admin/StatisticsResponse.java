package com.school.platform.dto.admin;

public record StatisticsResponse(
        long totalUsers,
        long totalCourses,
        long totalEnrollments,
        long pendingEnrollments
) {}
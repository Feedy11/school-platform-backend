package com.school.platform.controller;

import com.school.platform.dto.admin.AdminEnrollmentResponse;
import com.school.platform.dto.admin.StatisticsResponse;
import com.school.platform.dto.enrollment.EnrollmentRejectRequest;
import com.school.platform.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Tableau de bord (rôle ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/statistics")
    @Operation(summary = "Statistiques principales")
    public StatisticsResponse getStatistics() {
        return adminService.getStatistics();
    }

    @GetMapping("/enrollments")
    @Operation(summary = "Lister toutes les inscriptions")
    public List<AdminEnrollmentResponse> getEnrollments() {
        return adminService.getEnrollments();
    }

    @PutMapping("/enrollments/{id}/approve")
    @Operation(summary = "Approuver une inscription")
    public AdminEnrollmentResponse approveEnrollment(@PathVariable Long id) {
        return adminService.approveEnrollment(id);
    }

    @PutMapping("/enrollments/{id}/reject")
    @Operation(summary = "Rejeter une inscription")
    public AdminEnrollmentResponse rejectEnrollment(
            @PathVariable Long id,
            @RequestBody(required = false) EnrollmentRejectRequest request) {
        String reason = request != null && request.reason() != null ? request.reason() : null;
        return adminService.rejectEnrollment(id, reason);
    }

    @PutMapping("/enrollments/{id}/revoke")
    @Operation(summary = "Révoquer une inscription approuvée")
    public AdminEnrollmentResponse revokeEnrollment(
            @PathVariable Long id,
            @RequestBody(required = false) EnrollmentRejectRequest request) {
        String reason = request != null && request.reason() != null ? request.reason() : null;
        return adminService.revokeEnrollment(id, reason);
    }
}
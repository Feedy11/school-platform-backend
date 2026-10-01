package com.school.platform.controller;

import com.school.platform.dto.enrollment.EnrollmentRequest;
import com.school.platform.dto.enrollment.EnrollmentResponse;
import com.school.platform.dto.enrollment.ProgressRequest;
import com.school.platform.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Inscriptions et progression (rôle STUDENT)")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @Operation(summary = "S'inscrire à une formation")
    public ResponseEntity<EnrollmentResponse> enroll(
            @Valid @RequestBody EnrollmentRequest request,
            Authentication authentication) {
        EnrollmentResponse response =
                enrollmentService.enroll(authentication.getName(), request.courseId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-courses")
    @Operation(summary = "Lister mes formations avec ma progression")
    public List<EnrollmentResponse> getMyCourses(Authentication authentication) {
        return enrollmentService.getMyCourses(authentication.getName());
    }

    @PutMapping("/{id}/progress")
    @Operation(summary = "Mettre à jour ma progression (0 à 100)")
    public EnrollmentResponse updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody ProgressRequest request,
            Authentication authentication) {
        return enrollmentService.updateProgress(
                authentication.getName(), id, request.progress());
    }
}
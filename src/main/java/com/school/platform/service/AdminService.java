package com.school.platform.service;

import com.school.platform.dto.admin.AdminEnrollmentResponse;
import com.school.platform.dto.admin.StatisticsResponse;
import com.school.platform.entity.Enrollment;
import com.school.platform.entity.EnrollmentStatus;
import com.school.platform.exception.BadRequestException;
import com.school.platform.exception.ResourceNotFoundException;
import com.school.platform.repository.CourseRepository;
import com.school.platform.repository.EnrollmentRepository;
import com.school.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    public StatisticsResponse getStatistics() {
        return new StatisticsResponse(
                userRepository.count(),
                courseRepository.count(),
                enrollmentRepository.count(),
                enrollmentRepository.countByStatus(EnrollmentStatus.PENDING));
    }

    public List<AdminEnrollmentResponse> getEnrollments() {
        return enrollmentRepository.findAllByOrderByEnrolledAtDesc().stream()
                .map(AdminEnrollmentResponse::from)
                .toList();
    }

    @Transactional
    public AdminEnrollmentResponse approveEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inscription introuvable avec l'id " + enrollmentId));

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new BadRequestException("Seules les inscriptions en attente peuvent être approuvées");
        }

        enrollment.setStatus(EnrollmentStatus.APPROVED);
        enrollment.setReviewedAt(LocalDateTime.now());
        enrollment.setRejectionReason(null);
        enrollmentRepository.save(enrollment);

        // Create notification for the student
        notificationService.createNotification(
                enrollment.getUser(),
                "Enrollment Approved ✅",
                "Your enrollment in \"" + enrollment.getCourse().getTitle() + "\" has been approved. You can now start learning!",
                "ENROLLMENT_APPROVED",
                enrollment.getId()
        );

        return AdminEnrollmentResponse.from(enrollment);
    }

    @Transactional
    public AdminEnrollmentResponse rejectEnrollment(Long enrollmentId, String reason) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inscription introuvable avec l'id " + enrollmentId));

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new BadRequestException("Seules les inscriptions en attente peuvent être rejetées");
        }

        enrollment.setStatus(EnrollmentStatus.REJECTED);
        enrollment.setReviewedAt(LocalDateTime.now());
        enrollment.setRejectionReason(reason);
        enrollmentRepository.save(enrollment);

        // Create notification for the student
        String message = "Your enrollment in \"" + enrollment.getCourse().getTitle() + "\" has been rejected.";
        if (reason != null && !reason.isBlank()) {
            message += " Reason: " + reason;
        }

        notificationService.createNotification(
                enrollment.getUser(),
                "Enrollment Rejected ❌",
                message,
                "ENROLLMENT_REJECTED",
                enrollment.getId()
        );

        return AdminEnrollmentResponse.from(enrollment);
    }

    @Transactional
    public AdminEnrollmentResponse revokeEnrollment(Long enrollmentId, String reason) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inscription introuvable avec l'id " + enrollmentId));

        if (enrollment.getStatus() != EnrollmentStatus.APPROVED) {
            throw new BadRequestException("Seules les inscriptions approuvées peuvent être révoquées");
        }

        enrollment.setStatus(EnrollmentStatus.REJECTED);
        enrollment.setReviewedAt(LocalDateTime.now());
        enrollment.setRejectionReason(reason != null && !reason.isBlank() ? reason : "Access revoked by administrator");
        enrollmentRepository.save(enrollment);

        // Create notification for the student
        String message = "Your access to \"" + enrollment.getCourse().getTitle() + "\" has been revoked by an administrator.";
        if (reason != null && !reason.isBlank()) {
            message += " Reason: " + reason;
        }

        notificationService.createNotification(
                enrollment.getUser(),
                "Enrollment Revoked ⚠️",
                message,
                "ENROLLMENT_REVOKED",
                enrollment.getId()
        );

        return AdminEnrollmentResponse.from(enrollment);
    }
}
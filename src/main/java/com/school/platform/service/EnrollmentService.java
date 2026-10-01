package com.school.platform.service;

import com.school.platform.dto.enrollment.EnrollmentResponse;
import com.school.platform.entity.Course;
import com.school.platform.entity.Enrollment;
import com.school.platform.entity.EnrollmentStatus;
import com.school.platform.entity.User;
import com.school.platform.exception.BadRequestException;
import com.school.platform.exception.ConflictException;
import com.school.platform.exception.ResourceNotFoundException;
import com.school.platform.repository.CourseRepository;
import com.school.platform.repository.EnrollmentRepository;
import com.school.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public EnrollmentResponse enroll(String userEmail, Long courseId) {
        User user = getUser(userEmail);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Formation introuvable avec l'id " + courseId));

        // Check for existing enrollment
        Optional<Enrollment> existing = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId);

        if (existing.isPresent()) {
            Enrollment e = existing.get();
            if (e.getStatus() == EnrollmentStatus.REJECTED) {
                // Allow re-enrollment after rejection
                e.setStatus(EnrollmentStatus.PENDING);
                e.setProgress(0);
                e.setRejectionReason(null);
                e.setReviewedAt(null);
                return EnrollmentResponse.from(enrollmentRepository.save(e));
            }
            throw new ConflictException("Vous êtes déjà inscrit à cette formation");
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .progress(0)
                .build();
        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    public List<EnrollmentResponse> getMyCourses(String userEmail) {
        User user = getUser(userEmail);
        return enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(user.getId()).stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @Transactional
    public EnrollmentResponse updateProgress(String userEmail, Long enrollmentId, int progress) {
        User user = getUser(userEmail);
        Enrollment enrollment = enrollmentRepository.findByIdAndUserId(enrollmentId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inscription introuvable avec l'id " + enrollmentId));

        if (enrollment.getStatus() != EnrollmentStatus.APPROVED) {
            throw new BadRequestException(
                    "Vous ne pouvez mettre à jour la progression que pour une inscription approuvée");
        }

        enrollment.setProgress(progress);
        return EnrollmentResponse.from(enrollment);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }
}
package com.school.platform.repository;

import com.school.platform.entity.Enrollment;
import com.school.platform.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    @EntityGraph(attributePaths = "course")
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    @EntityGraph(attributePaths = "course")
    List<Enrollment> findByUserIdOrderByEnrolledAtDesc(Long userId);

    @EntityGraph(attributePaths = "course")
    Optional<Enrollment> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"user", "course"})
    List<Enrollment> findAllByOrderByEnrolledAtDesc();

    @EntityGraph(attributePaths = {"user", "course"})
    List<Enrollment> findByStatusOrderByEnrolledAtDesc(EnrollmentStatus status);

    long countByStatus(EnrollmentStatus status);
}
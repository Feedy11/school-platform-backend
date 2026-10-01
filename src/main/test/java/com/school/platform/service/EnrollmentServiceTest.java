package com.school.platform.service;

import com.school.platform.dto.enrollment.EnrollmentResponse;
import com.school.platform.entity.Course;
import com.school.platform.entity.Enrollment;
import com.school.platform.entity.User;
import com.school.platform.exception.ConflictException;
import com.school.platform.exception.ResourceNotFoundException;
import com.school.platform.repository.CourseRepository;
import com.school.platform.repository.EnrollmentRepository;
import com.school.platform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    private static final String EMAIL = "student@school.com";

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @InjectMocks private EnrollmentService service;

    private final User user = User.builder().id(1L).email(EMAIL).build();
    private final Course course = Course.builder().id(10L).title("Angular").build();

    @Test
    void enroll_createsEnrollmentWithZeroProgress() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EnrollmentResponse response = service.enroll(EMAIL, 10L);

        assertThat(response.progress()).isZero();
        assertThat(response.title()).isEqualTo("Angular");
    }

    @Test
    void enroll_throwsConflict_whenAlreadyEnrolled() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> service.enroll(EMAIL, 10L))
                .isInstanceOf(ConflictException.class);
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enroll_throwsNotFound_whenCourseMissing() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enroll(EMAIL, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProgress_updatesOwnEnrollment() {
        Enrollment enrollment = Enrollment.builder()
                .id(5L).user(user).course(course).progress(0).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(enrollmentRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(enrollment));

        EnrollmentResponse response = service.updateProgress(EMAIL, 5L, 75);

        assertThat(response.progress()).isEqualTo(75);
    }

    @Test
    void updateProgress_throwsNotFound_whenEnrollmentBelongsToSomeoneElse() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(enrollmentRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProgress(EMAIL, 5L, 75))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
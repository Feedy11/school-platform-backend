package com.school.platform.service;

import com.school.platform.dto.course.CourseRequest;
import com.school.platform.dto.course.CourseResponse;
import com.school.platform.entity.Course;
import com.school.platform.entity.Level;
import com.school.platform.exception.ResourceNotFoundException;
import com.school.platform.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @InjectMocks private CourseService service;

    @Test
    void create_savesCourseWithRequestData() {
        CourseRequest request = new CourseRequest(
                "Angular", "Apprendre Angular", "Frontend", Level.BEGINNER, 20, null);
        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse response = service.create(request);

        assertThat(response.title()).isEqualTo("Angular");
        assertThat(response.level()).isEqualTo(Level.BEGINNER);
        assertThat(response.duration()).isEqualTo(20);
    }

    @Test
    void delete_throwsNotFound_whenCourseMissing() {
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(courseRepository, never()).deleteById(any());
    }
}
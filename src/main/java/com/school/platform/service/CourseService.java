package com.school.platform.service;

import com.school.platform.dto.course.CourseRequest;
import com.school.platform.dto.course.CourseResponse;
import com.school.platform.entity.Course;
import com.school.platform.exception.ResourceNotFoundException;
import com.school.platform.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    public List<CourseResponse> findAll() {
        return courseRepository.findAll(Sort.by("id")).stream()
                .map(CourseResponse::from)
                .toList();
    }

    public CourseResponse findById(Long id) {
        return CourseResponse.from(getCourse(id));
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        Course course = new Course();
        applyRequest(course, request);
        return CourseResponse.from(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = getCourse(id);
        applyRequest(course, request);
        return CourseResponse.from(course);
    }

    @Transactional
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw notFound(id);
        }
        courseRepository.deleteById(id);
    }

    private Course getCourse(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Formation introuvable avec l'id " + id);
    }

    private void applyRequest(Course course, CourseRequest request) {
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setCategory(request.category());
        course.setLevel(request.level());
        course.setDuration(request.duration());
        course.setImageUrl(request.imageUrl());
    }
}
package com.school.platform.controller;

import com.school.platform.dto.course.CourseRequest;
import com.school.platform.dto.course.CourseResponse;
import com.school.platform.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Catalogue des formations (lecture publique, écriture ADMIN)")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Lister les formations (public)")
    public List<CourseResponse> getAll() {
        return courseService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une formation (public)")
    public CourseResponse getById(@PathVariable Long id) {
        return courseService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Créer une formation (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une formation (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public CourseResponse update(@PathVariable Long id,
                                 @Valid @RequestBody CourseRequest request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une formation (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
package com.school.platform.controller;

import com.school.platform.security.RestSecurityExceptionHandler;
import com.school.platform.dto.enrollment.EnrollmentResponse;
import com.school.platform.security.JwtAuthenticationFilter;
import com.school.platform.security.JwtService;
import com.school.platform.security.SecurityConfig;
import com.school.platform.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestSecurityExceptionHandler.class})
@WebMvcTest(EnrollmentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class EnrollmentControllerTest {

    private static final String EMAIL = "student@school.com";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private EnrollmentService enrollmentService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;

    private EnrollmentResponse response(int progress) {
        return new EnrollmentResponse(5L, 1L, "Angular", progress, LocalDateTime.now());
    }

    @Test
    void enroll_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void enroll_asAdmin_isForbidden() throws Exception {
        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "STUDENT")
    void enroll_asStudent_usesEmailFromAuthentication() throws Exception {
        when(enrollmentService.enroll(EMAIL, 1L)).thenReturn(response(0));

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.progress").value(0));

        verify(enrollmentService).enroll(EMAIL, 1L);
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "STUDENT")
    void getMyCourses_asStudent_returnsList() throws Exception {
        when(enrollmentService.getMyCourses(EMAIL)).thenReturn(List.of(response(40)));

        mockMvc.perform(get("/api/enrollments/my-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Angular"))
                .andExpect(jsonPath("$[0].progress").value(40));
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "STUDENT")
    void updateProgress_withValidValue_isOk() throws Exception {
        when(enrollmentService.updateProgress(EMAIL, 5L, 75)).thenReturn(response(75));

        mockMvc.perform(put("/api/enrollments/5/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"progress\":75}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").value(75));
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "STUDENT")
    void updateProgress_above100_isBadRequest() throws Exception {
        mockMvc.perform(put("/api/enrollments/5/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"progress\":150}"))
                .andExpect(status().isBadRequest());
    }
}
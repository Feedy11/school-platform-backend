package com.school.platform.controller;
import com.school.platform.security.RestSecurityExceptionHandler;
import com.school.platform.dto.admin.AdminEnrollmentResponse;
import com.school.platform.dto.admin.StatisticsResponse;
import com.school.platform.security.JwtAuthenticationFilter;
import com.school.platform.security.JwtService;
import com.school.platform.security.SecurityConfig;
import com.school.platform.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestSecurityExceptionHandler.class})
@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AdminService adminService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;

    @Test
    void statistics_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/statistics"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void statistics_asStudent_isForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/statistics"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void statistics_asAdmin_returnsCounts() throws Exception {
        when(adminService.getStatistics()).thenReturn(new StatisticsResponse(120, 15, 350));

        mockMvc.perform(get("/api/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(120))
                .andExpect(jsonPath("$.totalCourses").value(15))
                .andExpect(jsonPath("$.totalEnrollments").value(350));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void enrollments_asStudent_isForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/enrollments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void enrollments_asAdmin_returnsList() throws Exception {
        when(adminService.getEnrollments()).thenReturn(List.of(new AdminEnrollmentResponse(
                1L, 2L, "Sara Ben Ali", "sara@example.com",
                3L, "Angular", 75, LocalDateTime.now())));

        mockMvc.perform(get("/api/admin/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentEmail").value("sara@example.com"))
                .andExpect(jsonPath("$[0].courseTitle").value("Angular"))
                .andExpect(jsonPath("$[0].progress").value(75));
    }
    
}
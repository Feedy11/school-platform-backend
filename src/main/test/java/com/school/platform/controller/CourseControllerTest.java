package com.school.platform.controller;

import com.school.platform.security.RestSecurityExceptionHandler;
import com.school.platform.dto.course.CourseResponse;
import com.school.platform.entity.Level;
import com.school.platform.security.JwtAuthenticationFilter;
import com.school.platform.security.JwtService;
import com.school.platform.security.SecurityConfig;
import com.school.platform.service.CourseService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestSecurityExceptionHandler.class})
@WebMvcTest(CourseController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class CourseControllerTest {

    private static final String VALID_BODY = """
            {"title":"Angular","description":"Desc","category":"Frontend",
             "level":"BEGINNER","duration":20}""";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CourseService courseService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;

    @Test
    void getAll_isPublic() throws Exception {
        when(courseService.findAll()).thenReturn(List.of(new CourseResponse(
                1L, "Angular", "Desc", "Frontend", Level.BEGINNER, 20, null, LocalDateTime.now())));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Angular"));
    }

    @Test
    void create_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void create_asStudent_isForbidden() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_isCreated() throws Exception {
        when(courseService.create(org.mockito.ArgumentMatchers.any())).thenReturn(
                new CourseResponse(1L, "Angular", "Desc", "Frontend",
                        Level.BEGINNER, 20, null, LocalDateTime.now()));

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_withInvalidBody_isBadRequest() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","description":"x","category":"x",
                                 "level":"BEGINNER","duration":-5}"""))
                .andExpect(status().isBadRequest());
    }

        @Test
    void preflight_fromAllowedOrigin_isAccepted() throws Exception {
        mockMvc.perform(options("/api/courses")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void preflight_fromUnknownOrigin_isRejected() throws Exception {
        mockMvc.perform(options("/api/courses")
                        .header("Origin", "http://evil.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
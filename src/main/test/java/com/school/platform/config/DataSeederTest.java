package com.school.platform.config;

import com.school.platform.entity.Course;
import com.school.platform.entity.Role;
import com.school.platform.entity.User;
import com.school.platform.repository.CourseRepository;
import com.school.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<List<Course>> coursesCaptor;

    private DataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DataSeeder(userRepository, courseRepository, passwordEncoder,
                "Admin123!", "Student123!");
    }

    @Test
    void run_seedsUsersAndCourses_whenDatabaseIsEmpty() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(courseRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "hashed-" + inv.getArgument(0));

        seeder.run(null);

        verify(userRepository, times(2)).save(userCaptor.capture());
        List<User> users = userCaptor.getAllValues();
        assertThat(users).extracting(User::getEmail)
                .containsExactly("admin@school.com", "student@school.com");
        assertThat(users).extracting(User::getRole)
                .containsExactly(Role.ADMIN, Role.STUDENT);
        assertThat(users).allSatisfy(user ->
                assertThat(user.getPassword()).startsWith("hashed-"));

        verify(courseRepository).saveAll(coursesCaptor.capture());
        assertThat(coursesCaptor.getValue()).extracting(Course::getTitle)
                .containsExactly("Angular", "Spring Boot", "Java", "Python", "SQL");
    }

    @Test
    void run_doesNothing_whenAlreadySeeded() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(courseRepository.count()).thenReturn(5L);

        seeder.run(null);

        verify(userRepository, never()).save(any());
        verify(courseRepository, never()).saveAll(any());
    }

    @Test
    void run_seedsCoursesOnly_whenUsersExistButNoCourses() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(courseRepository.count()).thenReturn(0L);

        seeder.run(null);

        verify(userRepository, never()).save(any());
        verify(courseRepository).saveAll(coursesCaptor.capture());
        assertThat(coursesCaptor.getValue()).hasSize(5);
    }
}
package com.school.platform.config;

import com.school.platform.entity.Course;
import com.school.platform.entity.Level;
import com.school.platform.entity.Role;
import com.school.platform.entity.User;
import com.school.platform.repository.CourseRepository;
import com.school.platform.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    static final String ADMIN_EMAIL = "admin@school.com";
    static final String STUDENT_EMAIL = "student@school.com";

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String studentPassword;

    public DataSeeder(UserRepository userRepository,
                      CourseRepository courseRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.admin-password}") String adminPassword,
                      @Value("${app.seed.student-password}") String studentPassword) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.studentPassword = studentPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        createUserIfMissing("Admin", "School", ADMIN_EMAIL, adminPassword, Role.ADMIN);
        createUserIfMissing("Student", "Demo", STUDENT_EMAIL, studentPassword, Role.STUDENT);
        createCoursesIfNoneExist();
    }

    private void createUserIfMissing(String firstName, String lastName, String email,
                                     String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        userRepository.save(User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build());
        log.info("Seed : utilisateur {} créé ({})", email, role);
    }

    private void createCoursesIfNoneExist() {
        if (courseRepository.count() > 0) {
            return;
        }
        courseRepository.saveAll(List.of(
                course("Angular", "Apprendre Angular de zéro : composants, services et routing.",
                        "Frontend", Level.BEGINNER, 20),
                course("Spring Boot", "Construire une API REST sécurisée avec Spring Boot.",
                        "Backend", Level.INTERMEDIATE, 30),
                course("Java", "Les bases du langage Java et de la programmation objet.",
                        "Backend", Level.BEGINNER, 40),
                course("Python", "Découvrir Python et écrire ses premiers scripts.",
                        "Programming", Level.BEGINNER, 35),
                course("SQL", "Interroger et modéliser une base de données relationnelle.",
                        "Database", Level.BEGINNER, 15)));
        log.info("Seed : 5 formations créées");
    }

    private Course course(String title, String description, String category,
                          Level level, int duration) {
        return Course.builder()
                .title(title)
                .description(description)
                .category(category)
                .level(level)
                .duration(duration)
                .build();
    }
}
package com.school.platform.security;

import com.school.platform.entity.Role;
import com.school.platform.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "ZGV2T25seVNlY3JldEtleUNoYW5nZU1lSW5Qcm9kMDA=";
    private static final String OTHER_SECRET = "ZGV2T25seVNlY3JldEtleUNoYW5nZU1lSW5Qcm9kMDE=";

    private final User user = User.builder()
            .id(1L).email("student@school.com").role(Role.STUDENT).build();

    @Test
    void generateToken_thenExtractEmail() {
        JwtService service = new JwtService(SECRET, 3_600_000);

        String token = service.generateToken(user);

        assertThat(service.extractEmail(token)).isEqualTo("student@school.com");
    }

    @Test
    void extractEmail_rejectsTokenSignedWithAnotherKey() {
        JwtService service = new JwtService(SECRET, 3_600_000);
        String forgedToken = new JwtService(OTHER_SECRET, 3_600_000).generateToken(user);

        assertThatThrownBy(() -> service.extractEmail(forgedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractEmail_rejectsExpiredToken() {
        JwtService service = new JwtService(SECRET, -1_000);
        String expiredToken = service.generateToken(user);

        assertThatThrownBy(() -> service.extractEmail(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
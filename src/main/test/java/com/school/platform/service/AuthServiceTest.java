package com.school.platform.service;

import com.school.platform.dto.auth.AuthResponse;
import com.school.platform.dto.auth.LoginRequest;
import com.school.platform.dto.auth.RegisterRequest;
import com.school.platform.entity.Role;
import com.school.platform.entity.User;
import com.school.platform.exception.ConflictException;
import com.school.platform.repository.UserRepository;
import com.school.platform.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthService service;

    private final User user = User.builder()
            .id(1L).firstName("Fadi").lastName("Naffeti")
            .email("fadi@example.com").role(Role.STUDENT).build();

    @Test
    void register_hashesPasswordAndAssignsStudentRole() {
        when(userRepository.existsByEmail("fadi@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = service.register(
                new RegisterRequest("Fadi", "Naffeti", "Fadi@Example.com", "Password123!"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed");
        assertThat(captor.getValue().getRole()).isEqualTo(Role.STUDENT);
        assertThat(captor.getValue().getEmail()).isEqualTo("fadi@example.com");
        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyUsed() {
        when(userRepository.existsByEmail("fadi@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("Fadi", "Naffeti", "fadi@example.com", "Password123!")))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenAndUser() {
        when(userRepository.findByEmail("fadi@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = service.login(new LoginRequest("fadi@example.com", "Password123!"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.type()).isEqualTo("Bearer");
        assertThat(response.user().email()).isEqualTo("fadi@example.com");
    }

    @Test
    void login_propagatesBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> service.login(new LoginRequest("fadi@example.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).generateToken(any());
    }
}
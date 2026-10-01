package com.school.platform.service;

import com.school.platform.dto.auth.AuthResponse;
import com.school.platform.dto.auth.LoginRequest;
import com.school.platform.dto.auth.RegisterRequest;
import com.school.platform.entity.Role;
import com.school.platform.entity.User;
import com.school.platform.exception.ConflictException;
import com.school.platform.exception.ResourceNotFoundException;
import com.school.platform.repository.UserRepository;
import com.school.platform.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Cet email est déjà utilisé");
        }

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.STUDENT)
                .build();

        return buildResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        return AuthResponse.of(jwtService.generateToken(user), user);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
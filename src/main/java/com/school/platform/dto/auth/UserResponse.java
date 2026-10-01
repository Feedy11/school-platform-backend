package com.school.platform.dto.auth;

import com.school.platform.entity.Role;
import com.school.platform.entity.User;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole());
    }
}
package com.school.platform.dto.auth;

import com.school.platform.entity.User;

public record AuthResponse(String token, String type, UserResponse user) {

    private static final String TOKEN_TYPE = "Bearer";

    public static AuthResponse of(String token, User user) {
        return new AuthResponse(token, TOKEN_TYPE, UserResponse.from(user));
    }
}
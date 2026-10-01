package com.school.platform.exception;

import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

public record ApiError(int status, String message, LocalDateTime timestamp) {

    public static ApiError of(HttpStatusCode status, String message) {
        return new ApiError(status.value(), message, LocalDateTime.now());
    }
}
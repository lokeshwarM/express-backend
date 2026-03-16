package com.express.expressbackend.domain.auth;

import java.util.UUID;

public class AuthResponse {

    private UUID userId;
    private String email;
    private String token;

    public AuthResponse(UUID userId, String email, String token) {
        this.userId = userId;
        this.email = email;
        this.token = token;
    }

    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
}
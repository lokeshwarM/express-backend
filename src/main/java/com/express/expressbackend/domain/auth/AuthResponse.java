package com.express.expressbackend.domain.auth;

import java.util.UUID;

public class AuthResponse {

    private UUID userId;
    private String email;
    private String token;
    private String role;

    public AuthResponse(UUID userId, String email, String token, String role) {
        this.userId = userId;
        this.email = email;
        this.token = token;
        this.role = role;
    }

    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
    public String getRole() { return role; }
}
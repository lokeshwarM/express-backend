package com.express.expressbackend.domain.user;

import java.util.UUID;

public class UserProfileResponse {

    private UUID id;
    private String email;
    private String publicDisplayId;
    private UserRole role;

    public UserProfileResponse(UUID id, String email, String publicDisplayId, UserRole role) {
        this.id = id;
        this.email = email;
        this.publicDisplayId = publicDisplayId;
        this.role = role;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPublicDisplayId() { return publicDisplayId; }
    public UserRole getRole() { return role; }
}
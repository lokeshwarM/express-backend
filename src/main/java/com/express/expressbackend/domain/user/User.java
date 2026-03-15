package com.express.expressbackend.domain.user;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String publicDisplayId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public User() {}

    public UUID getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() {return passwordHash;}
    public void setPasswordHash(String passwordHash) {this.passwordHash = passwordHash;}

    public String getPublicDisplayId() { return publicDisplayId; }
    public void setPublicDisplayId(String publicDisplayId) { this.publicDisplayId = publicDisplayId; }

    public UserRole  getRole() { return role; }
    public void setRole(UserRole  role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public OffsetDateTime getCreatedAt() { return createdAt; }


}
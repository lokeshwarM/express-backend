package com.express.expressbackend.domain.listener;

import com.express.expressbackend.domain.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "listeners")
public class Listener {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private boolean blacklisted;

    @Column(nullable = false)
    private int redFlagCount;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Listener() {}

    // ===== Getters =====

    public UUID getId() { return id; }

    public User getUser() { return user; }

    public boolean isAvailable() { return available; }

    public boolean isBlacklisted() { return blacklisted; }

    public int getRedFlagCount() { return redFlagCount; }

    public OffsetDateTime getCreatedAt() { return createdAt; }

    // ===== Setters =====

    public void setUser(User user) { this.user = user; }

    public void setAvailable(boolean available) { this.available = available; }

    public void setBlacklisted(boolean blacklisted) { this.blacklisted = blacklisted; }

    public void setRedFlagCount(int redFlagCount) { this.redFlagCount = redFlagCount; }

    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}
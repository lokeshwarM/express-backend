package com.express.expressbackend.domain.listener;

import com.express.expressbackend.domain.user.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "listeners")
public class Listener {

    @Id
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

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    public Listener() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public boolean isBlacklisted() { return blacklisted; }
    public void setBlacklisted(boolean blacklisted) { this.blacklisted = blacklisted; }

    public int getRedFlagCount() { return redFlagCount; }
    public void setRedFlagCount(int redFlagCount) { this.redFlagCount = redFlagCount; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
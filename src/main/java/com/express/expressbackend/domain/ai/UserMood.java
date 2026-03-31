package com.express.expressbackend.domain.ai;

import com.express.expressbackend.domain.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_moods")
public class UserMood {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    // stressed / anxious / neutral / good
    @Column(nullable = false)
    private String mood;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getMood() { return mood; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setUser(User user) { this.user = user; }
    public void setMood(String mood) { this.mood = mood; }
}
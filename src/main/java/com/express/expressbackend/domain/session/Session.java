package com.express.expressbackend.domain.session;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Listener listener;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    private Instant startedAt;
    private Instant endedAt;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public void setUser(User user) {
        this.user = user;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Listener getListener() {
        return listener;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public SessionType getType() {
        return type;
    }

    public void setType(SessionType type) {
        this.type = type;
    }    
}

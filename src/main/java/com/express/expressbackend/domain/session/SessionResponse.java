package com.express.expressbackend.domain.session;

import java.time.Instant;
import java.util.UUID;

public class SessionResponse {

    private UUID id;
    private UUID userId;
    private UUID listenerId;
    private SessionStatus status;
    private Instant startedAt;
    private Instant endedAt;

    public SessionResponse(UUID id,
                           UUID userId,
                           UUID listenerId,
                           SessionStatus status,
                           Instant startedAt,
                           Instant endedAt) {
        this.id = id;
        this.userId = userId;
        this.listenerId = listenerId;
        this.status = status;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getListenerId() { return listenerId; }
    public SessionStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
}
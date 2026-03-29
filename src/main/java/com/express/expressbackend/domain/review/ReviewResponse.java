package com.express.expressbackend.domain.review;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ReviewResponse {
    private UUID id;
    private int rating;
    private String comment;
    private String userDisplayId;
    private UUID sessionId;
    private OffsetDateTime createdAt;

    public ReviewResponse(UUID id, int rating, String comment,
                          String userDisplayId, UUID sessionId,
                          OffsetDateTime createdAt) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.userDisplayId = userDisplayId;
        this.sessionId = sessionId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getUserDisplayId() { return userDisplayId; }
    public UUID getSessionId() { return sessionId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
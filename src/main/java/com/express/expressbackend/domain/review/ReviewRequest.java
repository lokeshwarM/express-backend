package com.express.expressbackend.domain.review;

import java.util.UUID;

public class ReviewRequest {
    private UUID sessionId;
    private int rating;      // 1-5
    private String comment;

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
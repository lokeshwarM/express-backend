package com.express.expressbackend.domain.session;

import java.util.UUID;

public class CreateSessionRequest {

    private UUID userId;
    private UUID listenerId;
    private SessionType type;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getListenerId() {
        return listenerId;
    }

    public void setListenerId(UUID listenerId) {
        this.listenerId = listenerId;
    }

    public SessionType getType() {
        return type;
    }

    public void setType(SessionType type) {
        this.type = type;
    }
}
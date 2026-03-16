package com.express.expressbackend.domain.session;

import java.util.UUID;

public class CreateSessionRequest {

    private UUID listenerId;
    private SessionType type;


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
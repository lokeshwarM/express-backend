package com.express.expressbackend.domain.session;

public class CreateSessionRequest {

    private SessionType type;

    public SessionType getType() {
        return type;
    }

    public void setType(SessionType type) {
        this.type = type;
    }
}
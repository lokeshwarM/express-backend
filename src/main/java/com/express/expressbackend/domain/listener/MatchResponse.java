package com.express.expressbackend.domain.listener;

import java.util.UUID;

public class MatchResponse {

    private UUID listenerId;

    public MatchResponse(UUID listenerId) {
        this.listenerId = listenerId;
    }

    public UUID getListenerId() {
        return listenerId;
    }
}
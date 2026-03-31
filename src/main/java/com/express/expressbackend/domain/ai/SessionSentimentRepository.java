package com.express.expressbackend.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionSentimentRepository extends JpaRepository<SessionSentiment, UUID> {
    Optional<SessionSentiment> findBySessionId(UUID sessionId);
}
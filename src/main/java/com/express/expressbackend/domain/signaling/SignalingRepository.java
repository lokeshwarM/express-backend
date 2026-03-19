package com.express.expressbackend.domain.signaling;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SignalingRepository extends JpaRepository<SignalingMessage, UUID> {
    Optional<SignalingMessage> findBySessionId(UUID sessionId);
}
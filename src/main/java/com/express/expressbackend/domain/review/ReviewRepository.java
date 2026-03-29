package com.express.expressbackend.domain.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByListenerIdOrderByCreatedAtDesc(UUID listenerId);
    Optional<Review> findBySessionId(UUID sessionId);
}
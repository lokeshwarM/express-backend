package com.express.expressbackend.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserMoodRepository extends JpaRepository<UserMood, UUID> {
    // Get the latest mood for a user
    Optional<UserMood> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
package com.express.expressbackend.domain.session;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByListenerId(UUID listenerId);

    List<Session> findByUserId(UUID userId);

    Optional<Session> findByUserIdAndStatus(UUID userId, SessionStatus status);
}
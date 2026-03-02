package com.express.expressbackend.domain.session;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByListenerId(UUID listenerId);

    List<Session> findByUserId(UUID userId);
}
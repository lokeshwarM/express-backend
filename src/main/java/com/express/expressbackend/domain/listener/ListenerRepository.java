package com.express.expressbackend.domain.listener;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ListenerRepository extends JpaRepository<Listener, UUID> {

    Optional<Listener> findByUserId(UUID userId);
}
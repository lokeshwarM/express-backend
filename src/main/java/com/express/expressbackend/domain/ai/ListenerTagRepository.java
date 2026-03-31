package com.express.expressbackend.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ListenerTagRepository extends JpaRepository<ListenerTag, UUID> {
    List<ListenerTag> findByListenerId(UUID listenerId);
    void deleteByListenerId(UUID listenerId);
}
package com.express.expressbackend.domain.listener;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ListenerRepository extends JpaRepository<Listener, UUID> {

    Optional<Listener> findByUserId(UUID userId);
    List<Listener> findByAvailableTrueAndBlacklistedFalse();

    @Query(value = """
    SELECT *
    FROM listeners
    WHERE available = true
    AND blacklisted = false
    ORDER BY random()
    LIMIT 1
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)

    Listener findRandomAvailableListener();
}
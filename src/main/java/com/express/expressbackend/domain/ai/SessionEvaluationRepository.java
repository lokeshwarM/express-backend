package com.express.expressbackend.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionEvaluationRepository extends JpaRepository<SessionEvaluation, UUID> {
    Optional<SessionEvaluation> findBySessionId(UUID sessionId);
    List<SessionEvaluation> findBySessionListenerId(UUID listenerId);
    List<SessionEvaluation> findByAnomalyTrue();
}
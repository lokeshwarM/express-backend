package com.express.expressbackend.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserTagRepository extends JpaRepository<UserTag, UUID> {
    List<UserTag> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
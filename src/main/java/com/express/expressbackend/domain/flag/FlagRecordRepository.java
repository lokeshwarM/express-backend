package com.express.expressbackend.domain.flag;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FlagRecordRepository extends JpaRepository<FlagRecord, UUID> {

    List<FlagRecord> findByListenerId(UUID listenerId);
}
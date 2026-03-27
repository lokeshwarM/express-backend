package com.express.expressbackend.domain.otp;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<OtpRecord, UUID> {

    Optional<OtpRecord> findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
            String email, OtpType type);

    @Transactional
    void deleteByEmailAndType(String email, OtpType type);
}
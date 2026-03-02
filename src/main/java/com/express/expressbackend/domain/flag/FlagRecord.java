package com.express.expressbackend.domain.flag;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.session.Session;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "flag_records")
public class FlagRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    private Listener listener;

    @ManyToOne(optional = false)
    private Session session;

    @Column(nullable = false)
    private String reason; // PERSONAL_INFO_SHARED

    @Column(nullable = false)
    private double confidenceScore;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // getters & setters
}

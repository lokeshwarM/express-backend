package com.express.expressbackend.domain.ai;

import com.express.expressbackend.domain.session.Session;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_evaluations")
public class SessionEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "session_id", unique = true)
    private Session session;

    // 1-10 overall effectiveness score
    @Column(nullable = false)
    private double effectivenessScore;

    // duration in seconds — longer = more engaged
    @Column(nullable = false)
    private long durationSeconds;

    // did user give a high rating (>=4)?
    @Column(nullable = false)
    private boolean positiveOutcome;

    // was it flagged during session?
    @Column(nullable = false)
    private boolean flagged;

    // was it anomalous (too short, flagged, bad rating)?
    @Column(nullable = false)
    private boolean anomaly;

    // engagement level: low / medium / high
    @Column(nullable = false)
    private String engagementLevel;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public Session getSession() { return session; }
    public double getEffectivenessScore() { return effectivenessScore; }
    public long getDurationSeconds() { return durationSeconds; }
    public boolean isPositiveOutcome() { return positiveOutcome; }
    public boolean isFlagged() { return flagged; }
    public boolean isAnomaly() { return anomaly; }
    public String getEngagementLevel() { return engagementLevel; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setSession(Session session) { this.session = session; }
    public void setEffectivenessScore(double effectivenessScore) { this.effectivenessScore = effectivenessScore; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
    public void setPositiveOutcome(boolean positiveOutcome) { this.positiveOutcome = positiveOutcome; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
    public void setAnomaly(boolean anomaly) { this.anomaly = anomaly; }
    public void setEngagementLevel(String engagementLevel) { this.engagementLevel = engagementLevel; }
}
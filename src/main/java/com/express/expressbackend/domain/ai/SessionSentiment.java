package com.express.expressbackend.domain.ai;

import com.express.expressbackend.domain.session.Session;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_sentiments")
public class SessionSentiment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "session_id", unique = true)
    private Session session;

    // positive / negative / neutral
    @Column(nullable = false)
    private String sentiment;

    // 0.0 to 1.0
    @Column(nullable = false)
    private double confidenceScore;

    // e.g. "stress, work pressure, felt unheard"
    @Column(length = 500)
    private String keyTopics;

    // 1-10
    @Column(nullable = false)
    private int satisfactionScore;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public Session getSession() { return session; }
    public String getSentiment() { return sentiment; }
    public double getConfidenceScore() { return confidenceScore; }
    public String getKeyTopics() { return keyTopics; }
    public int getSatisfactionScore() { return satisfactionScore; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setSession(Session session) { this.session = session; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    public void setKeyTopics(String keyTopics) { this.keyTopics = keyTopics; }
    public void setSatisfactionScore(int satisfactionScore) { this.satisfactionScore = satisfactionScore; }
}
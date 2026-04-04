package com.express.expressbackend.domain.ai;

import com.express.expressbackend.domain.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_memories")
public class UserMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // Total sessions completed
    @Column(nullable = false)
    private int totalSessions = 0;

    // Average satisfaction score across all sessions (1-10)
    @Column(nullable = false)
    private double avgSatisfactionScore = 5.0;

    // Dominant emotion across sessions: stressed/anxious/neutral/positive
    @Column
    private String dominantEmotion;

    // Comma-separated recurring topics: "stress,career,relationships"
    @Column(length = 500)
    private String recurringTopics;

    // Is stress recurring (true) or situational (false)?
    @Column(nullable = false)
    private boolean recurringStress = false;

    // Last session sentiment
    @Column
    private String lastSessionSentiment;

    // Trend: improving / declining / stable
    @Column
    private String emotionalTrend = "stable";

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public int getTotalSessions() { return totalSessions; }
    public double getAvgSatisfactionScore() { return avgSatisfactionScore; }
    public String getDominantEmotion() { return dominantEmotion; }
    public String getRecurringTopics() { return recurringTopics; }
    public boolean isRecurringStress() { return recurringStress; }
    public String getLastSessionSentiment() { return lastSessionSentiment; }
    public String getEmotionalTrend() { return emotionalTrend; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setUser(User user) { this.user = user; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
    public void setAvgSatisfactionScore(double avgSatisfactionScore) { this.avgSatisfactionScore = avgSatisfactionScore; }
    public void setDominantEmotion(String dominantEmotion) { this.dominantEmotion = dominantEmotion; }
    public void setRecurringTopics(String recurringTopics) { this.recurringTopics = recurringTopics; }
    public void setRecurringStress(boolean recurringStress) { this.recurringStress = recurringStress; }
    public void setLastSessionSentiment(String lastSessionSentiment) { this.lastSessionSentiment = lastSessionSentiment; }
    public void setEmotionalTrend(String emotionalTrend) { this.emotionalTrend = emotionalTrend; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
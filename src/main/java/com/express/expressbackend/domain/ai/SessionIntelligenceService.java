package com.express.expressbackend.domain.ai;

import com.express.expressbackend.domain.flag.FlagRecordRepository;
import com.express.expressbackend.domain.review.Review;
import com.express.expressbackend.domain.review.ReviewRepository;
import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.user.User;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SessionIntelligenceService {

    private final SessionEvaluationRepository evaluationRepository;
    private final SessionSentimentRepository sentimentRepository;
    private final UserMemoryRepository userMemoryRepository;
    private final ReviewRepository reviewRepository;
    private final FlagRecordRepository flagRecordRepository;

    public SessionIntelligenceService(
            SessionEvaluationRepository evaluationRepository,
            SessionSentimentRepository sentimentRepository,
            UserMemoryRepository userMemoryRepository,
            ReviewRepository reviewRepository,
            FlagRecordRepository flagRecordRepository) {
        this.evaluationRepository = evaluationRepository;
        this.sentimentRepository = sentimentRepository;
        this.userMemoryRepository = userMemoryRepository;
        this.reviewRepository = reviewRepository;
        this.flagRecordRepository = flagRecordRepository;
    }

    //  Phase 4 — Called after every session ends
    // Runs async so it doesn't slow down the endSession response
    @Async
    @Transactional
    public void evaluateSession(Session session) {
        // Skip if already evaluated
        if (evaluationRepository.findBySessionId(session.getId()).isPresent()) return;

        long durationSeconds = 0;
        if (session.getStartedAt() != null && session.getEndedAt() != null) {
            durationSeconds = Duration.between(session.getStartedAt(), session.getEndedAt()).getSeconds();
        }

        // Get review if exists
        Optional<Review> review = reviewRepository.findBySessionId(session.getId());
        int rating = review.map(Review::getRating).orElse(0);
        boolean positiveOutcome = rating >= 4;

        // Check if flagged
        boolean flagged = !flagRecordRepository.findByListenerId(
                session.getListener().getId()).stream()
                .filter(f -> f.getSession().getId().equals(session.getId()))
                .collect(Collectors.toList()).isEmpty();

        // Get sentiment
        Optional<SessionSentiment> sentiment = sentimentRepository.findBySessionId(session.getId());
        int satisfactionScore = sentiment.map(SessionSentiment::getSatisfactionScore).orElse(5);

        //  Effectiveness score calculation
        double score = 5.0; // base

        // Duration factor: <2min = low, 2-10min = medium, >10min = high
        if (durationSeconds >= 600) score += 2.0;
        else if (durationSeconds >= 120) score += 1.0;
        else score -= 1.0;

        // Rating factor
        if (rating >= 4) score += 2.0;
        else if (rating == 3) score += 0.5;
        else if (rating > 0 && rating <= 2) score -= 2.0;

        // Sentiment factor
        if (satisfactionScore >= 7) score += 1.0;
        else if (satisfactionScore <= 3) score -= 1.0;

        // Flag penalty
        if (flagged) score -= 3.0;

        score = Math.max(1.0, Math.min(10.0, score));

        //  Anomaly detection
        boolean anomaly = flagged
                || (durationSeconds < 30 && durationSeconds > 0) // too short
                || (rating > 0 && rating <= 2)                   // very bad rating
                || satisfactionScore <= 2;                        // very low satisfaction

        //  Engagement level
        String engagementLevel;
        if (durationSeconds >= 600) engagementLevel = "high";
        else if (durationSeconds >= 120) engagementLevel = "medium";
        else engagementLevel = "low";

        SessionEvaluation evaluation = new SessionEvaluation();
        evaluation.setSession(session);
        evaluation.setEffectivenessScore(Math.round(score * 10.0) / 10.0);
        evaluation.setDurationSeconds(durationSeconds);
        evaluation.setPositiveOutcome(positiveOutcome);
        evaluation.setFlagged(flagged);
        evaluation.setAnomaly(anomaly);
        evaluation.setEngagementLevel(engagementLevel);
        evaluationRepository.save(evaluation);

        //  Phase 5 — Update user memory
        updateUserMemory(session.getUser(), sentiment.orElse(null), satisfactionScore);
    }

    //  Phase 5 — Update long-term user memory profile
    private void updateUserMemory(User user, SessionSentiment sentiment, int satisfactionScore) {

        UserMemory memory = userMemoryRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserMemory m = new UserMemory();
                    m.setUser(user);
                    return m;
                });

        // Update session count
        memory.setTotalSessions(memory.getTotalSessions() + 1);

        // Update rolling average satisfaction
        double currentAvg = memory.getAvgSatisfactionScore();
        int total = memory.getTotalSessions();
        double newAvg = ((currentAvg * (total - 1)) + satisfactionScore) / total;
        memory.setAvgSatisfactionScore(Math.round(newAvg * 10.0) / 10.0);

        if (sentiment != null) {
            // Update last session sentiment
            memory.setLastSessionSentiment(sentiment.getSentiment());

            // Update dominant emotion (most recent wins with weight)
            updateDominantEmotion(memory, sentiment.getSentiment());

            // Update recurring topics
            updateRecurringTopics(memory, sentiment.getKeyTopics());

            // Detect recurring stress (3+ sessions with negative sentiment)
            if ("negative".equals(sentiment.getSentiment())) {
                long negativeSessions = userMemoryRepository.findByUserId(user.getId())
                        .map(m -> m.getTotalSessions()).orElse(0);
                // Simple heuristic: if avg satisfaction < 5 and dominantEmotion is stressed/anxious
                boolean recurringStress = memory.getAvgSatisfactionScore() < 5.0
                        && ("stressed".equals(memory.getDominantEmotion())
                            || "anxious".equals(memory.getDominantEmotion()));
                memory.setRecurringStress(recurringStress);
            }

            //  Emotional trend: compare current satisfaction to historical avg
            if (satisfactionScore > memory.getAvgSatisfactionScore() + 1) {
                memory.setEmotionalTrend("improving");
            } else if (satisfactionScore < memory.getAvgSatisfactionScore() - 1) {
                memory.setEmotionalTrend("declining");
            } else {
                memory.setEmotionalTrend("stable");
            }
        }

        memory.setUpdatedAt(OffsetDateTime.now());
        userMemoryRepository.save(memory);
    }

    private void updateDominantEmotion(UserMemory memory, String currentSentiment) {
        // Map sentiment to emotion
        String emotion = switch (currentSentiment) {
            case "negative" -> "stressed";
            case "positive" -> "positive";
            default -> "neutral";
        };
        memory.setDominantEmotion(emotion);
    }

    private void updateRecurringTopics(UserMemory memory, String newTopics) {
        if (newTopics == null || newTopics.isBlank()) return;

        String existing = memory.getRecurringTopics();
        if (existing == null || existing.isBlank()) {
            memory.setRecurringTopics(newTopics);
            return;
        }

        // Merge and keep top unique topics
        Set<String> combined = new LinkedHashSet<>();
        Arrays.stream(existing.split(",")).map(String::trim).forEach(combined::add);
        Arrays.stream(newTopics.split(",")).map(String::trim).forEach(combined::add);

        // Keep max 10 topics
        String merged = combined.stream().limit(10).collect(Collectors.joining(", "));
        memory.setRecurringTopics(merged);
    }
}
package com.express.expressbackend.domain.ai;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.session.SessionRepository;
import com.express.expressbackend.domain.session.SessionStatus;
import org.springframework.stereotype.Service;
import com.express.expressbackend.domain.ai.SessionEvaluationRepository;
import com.express.expressbackend.domain.ai.UserMemoryRepository;
import com.express.expressbackend.domain.ai.UserMemory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SmartMatchingService {

    private final ListenerRepository listenerRepository;
    private final ListenerTagRepository listenerTagRepository;
    private final SessionRepository sessionRepository;
    private final SessionEvaluationRepository evaluationRepository;
    private final UserMemoryRepository userMemoryRepository;

    public SmartMatchingService(ListenerRepository listenerRepository,
                                ListenerTagRepository listenerTagRepository,
                                SessionRepository sessionRepository,
                                SessionEvaluationRepository evaluationRepository,
                                UserMemoryRepository userMemoryRepository
                            ) {
        this.listenerRepository = listenerRepository;
        this.listenerTagRepository = listenerTagRepository;
        this.sessionRepository = sessionRepository;
        this.evaluationRepository = evaluationRepository;
        this.userMemoryRepository = userMemoryRepository;
    }

    // Core smart matching — finds best listener for user
    // Parameters: user's tags, user's current mood
    public Listener findBestMatch(List<String> userTags, String userMood) {

        List<Listener> available = listenerRepository.findByAvailableTrueAndBlacklistedFalse();

        if (available.isEmpty()) return null;

        // If no tags or mood — fall back to random from available
        if (userTags.isEmpty() && (userMood == null || userMood.isBlank())) {
            return available.get(new Random().nextInt(available.size()));
        }

        // Score each listener
        Listener best = null;
        double bestScore = -1;

        for (Listener listener : available) {

            double score = 0;

            // ── Tag overlap score (0 to 40 points) ──
            List<String> listenerTags = listenerTagRepository
                .findByListenerId(listener.getId())
                .stream()
                .map(ListenerTag::getTag)
                .collect(Collectors.toList());

            long tagMatches = userTags.stream()
                .filter(listenerTags::contains)
                .count();

            if (!userTags.isEmpty()) {
                score += ((double) tagMatches / userTags.size()) * 40;
            }

            // ── Rating score (0 to 30 points) ──
            // averageRating is 1.0 to 5.0 → normalize to 0-30
            score += ((listener.getAverageRating() - 1.0) / 4.0) * 30;

            // ── Flag penalty (0 to -20 points) ──
            score -= listener.getRedFlagCount() * 5;

            // ── Mood compatibility score (0 to 20 points) ──
            // If user is stressed/anxious, prefer listeners with "mental health" or "stress" tags
            if (userMood != null) {
                score += getMoodCompatibilityScore(userMood, listenerTags);
            }

            // ── Experience score (0 to 10 points) ──
            long completedSessions = sessionRepository
                .findByListenerId(listener.getId())
                .stream()
                .filter(s -> s.getStatus() == SessionStatus.ENDED)
                .count();
                
            // If user has recurring stress, prefer listeners who had high effectiveness with similar users
            if (userMood != null && (userMood.equals("stressed") || userMood.equals("anxious"))) {
                double avgEffectiveness = evaluationRepository
                    .findBySessionListenerId(listener.getId())
                    .stream()
                    .mapToDouble(com.express.expressbackend.domain.ai.SessionEvaluation::getEffectivenessScore)
                    .average()
                    .orElse(5.0);
                // Normalize 1-10 score to 0-10 points
                score += ((avgEffectiveness - 1.0) / 9.0) * 10;
            }

            if (score > bestScore) {
                bestScore = score;
                best = listener;
            }
        }

        return best != null ? best : available.get(0);
    }

    // Mood → tag affinity mapping
    private double getMoodCompatibilityScore(String mood, List<String> listenerTags) {
        Map<String, List<String>> moodTagAffinity = Map.of(
            "stressed", List.of("stress", "mental health", "mindfulness", "relaxation"),
            "anxious", List.of("anxiety", "mental health", "calm", "mindfulness"),
            "neutral", List.of("general", "motivation", "life advice"),
            "good", List.of("motivation", "career", "growth", "goals")
        );

        List<String> preferredTags = moodTagAffinity.getOrDefault(
            mood.toLowerCase(), List.of()
        );

        long matches = preferredTags.stream()
            .filter(listenerTags::contains)
            .count();

        if (preferredTags.isEmpty()) return 0;
        return ((double) matches / preferredTags.size()) * 20;
    }
}
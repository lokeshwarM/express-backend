package com.express.expressbackend.api;

import com.express.expressbackend.domain.ai.*;
import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.common.AuthUtil;
import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.review.ReviewRepository;
import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.session.SessionRepository;
import com.express.expressbackend.domain.session.SessionStatus;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final UserRepository userRepository;
    private final ListenerRepository listenerRepository;
    private final UserTagRepository userTagRepository;
    private final ListenerTagRepository listenerTagRepository;
    private final UserMoodRepository userMoodRepository;
    private final SessionSentimentRepository sentimentRepository;
    private final SessionRepository sessionRepository;
    private final ReviewRepository reviewRepository;
    private final OpenAiService openAiService;
    private final SessionEvaluationRepository evaluationRepository;
    private final UserMemoryRepository userMemoryRepository;

    public AiController(UserRepository userRepository,
                        ListenerRepository listenerRepository,
                        UserTagRepository userTagRepository,
                        ListenerTagRepository listenerTagRepository,
                        UserMoodRepository userMoodRepository,
                        SessionSentimentRepository sentimentRepository,
                        SessionRepository sessionRepository,
                        ReviewRepository reviewRepository,
                        OpenAiService openAiService,
                        SessionEvaluationRepository evaluationRepository,
                        UserMemoryRepository userMemoryRepository
                    ) {
        this.userRepository = userRepository;
        this.listenerRepository = listenerRepository;
        this.userTagRepository = userTagRepository;
        this.listenerTagRepository = listenerTagRepository;
        this.userMoodRepository = userMoodRepository;
        this.sentimentRepository = sentimentRepository;
        this.sessionRepository = sessionRepository;
        this.reviewRepository = reviewRepository;
        this.openAiService = openAiService;
        this.evaluationRepository=evaluationRepository;
        this.userMemoryRepository=userMemoryRepository;
    }


    // USER TAGS

    // Get current user's tags
    @GetMapping("/user/tags")
    public ApiResponse<List<String>> getUserTags() {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> tags = userTagRepository.findByUserId(user.getId())
                .stream().map(UserTag::getTag).collect(Collectors.toList());

        return new ApiResponse<>(tags);
    }

    // Save user tags (replaces existing)
    @PostMapping("/user/tags")
    @Transactional
    public ApiResponse<String> saveUserTags(@RequestBody List<String> tags) {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete old tags
        userTagRepository.deleteByUserId(user.getId());

        // Save new ones (max 5)
        tags.stream().limit(5).forEach(tag -> {
            UserTag ut = new UserTag();
            ut.setUser(user);
            ut.setTag(tag.toLowerCase().trim());
            userTagRepository.save(ut);
        });

        return new ApiResponse<>("Tags saved");
    }

    // LISTENER TAGS

    @GetMapping("/listener/tags")
    public ApiResponse<List<String>> getListenerTags() {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Listener listener = listenerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Listener not found"));

        List<String> tags = listenerTagRepository.findByListenerId(listener.getId())
                .stream().map(ListenerTag::getTag).collect(Collectors.toList());

        return new ApiResponse<>(tags);
    }

    @PostMapping("/listener/tags")
    @Transactional
    public ApiResponse<String> saveListenerTags(@RequestBody List<String> tags) {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Listener listener = listenerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Listener not found"));

        listenerTagRepository.deleteByListenerId(listener.getId());

        tags.stream().limit(5).forEach(tag -> {
            ListenerTag lt = new ListenerTag();
            lt.setListener(listener);
            lt.setTag(tag.toLowerCase().trim());
            listenerTagRepository.save(lt);
        });

        return new ApiResponse<>("Tags saved");
    }

  
    // USER MOOD (pre-session)  

    @PostMapping("/user/mood")
    public ApiResponse<String> saveMood(@RequestBody Map<String, String> body) {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String mood = body.get("mood");
        if (mood == null || mood.isBlank()) {
            throw new RuntimeException("Mood is required");
        }

        UserMood userMood = new UserMood();
        userMood.setUser(user);
        userMood.setMood(mood.toLowerCase().trim());
        userMoodRepository.save(userMood);

        return new ApiResponse<>("Mood saved");
    }

    @GetMapping("/user/mood")
    public ApiResponse<String> getLatestMood() {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMoodRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .map(m -> new ApiResponse<>(m.getMood()))
                .orElse(new ApiResponse<>("neutral"));
    }

    // POST-SESSION SENTIMENT ANALYSIS 
  

    // Called after user submits a review — analyzes the comment with OpenAI
    @PostMapping("/session/{sessionId}/analyze")
    @Transactional
    public ApiResponse<Map<String, Object>> analyzeSession(
            @PathVariable UUID sessionId,
            @RequestBody Map<String, String> body) {

        // Check session exists and is ended
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.ENDED) {
            throw new RuntimeException("Can only analyze ended sessions");
        }

        // If already analyzed — return existing
        if (sentimentRepository.findBySessionId(sessionId).isPresent()) {
            SessionSentiment existing = sentimentRepository.findBySessionId(sessionId).get();
            return new ApiResponse<>(Map.of(
                "sentiment", existing.getSentiment(),
                "confidenceScore", existing.getConfidenceScore(),
                "keyTopics", existing.getKeyTopics(),
                "satisfactionScore", existing.getSatisfactionScore()
            ));
        }

        String reviewText = body.getOrDefault("reviewText", "");

        // If no review text — use a default neutral analysis
        if (reviewText.isBlank()) {
            SessionSentiment neutral = new SessionSentiment();
            neutral.setSession(session);
            neutral.setSentiment("neutral");
            neutral.setConfidenceScore(0.5);
            neutral.setKeyTopics("");
            neutral.setSatisfactionScore(5);
            sentimentRepository.save(neutral);
            return new ApiResponse<>(Map.of(
                "sentiment", "neutral",
                "confidenceScore", 0.5,
                "keyTopics", "",
                "satisfactionScore", 5
            ));
        }

        // Call OpenAI
        OpenAiService.SentimentResult result = openAiService.analyzeSentiment(reviewText);

        SessionSentiment sentiment = new SessionSentiment();
        sentiment.setSession(session);
        sentiment.setSentiment(result.sentiment);
        sentiment.setConfidenceScore(result.confidenceScore);
        sentiment.setKeyTopics(result.keyTopics);
        sentiment.setSatisfactionScore(result.satisfactionScore);
        sentimentRepository.save(sentiment);

        return new ApiResponse<>(Map.of(
                "sentiment", result.sentiment,
                "confidenceScore", result.confidenceScore,
                "keyTopics", result.keyTopics,
                "satisfactionScore", result.satisfactionScore
        ));
    }

    
    // PHASE 3 — REAL-TIME AI SUGGESTIONS FOR LISTENER

    @Autowired
    private AiSuggestionService aiSuggestionService;

    // Called every 60s from listener's browser with transcript snippet
    @PostMapping("/session/{sessionId}/suggest")
    public ApiResponse<Map<String, Object>> getSuggestions(
            @PathVariable UUID sessionId,
            @RequestBody Map<String, String> body) {

        String transcript = body.getOrDefault("transcript", "");
        String userMood = body.getOrDefault("userMood", "neutral");

        //  Critical state check — fast, no AI call needed
        boolean isCritical = aiSuggestionService.isCriticalState(transcript);

        if (isCritical) {
            return new ApiResponse<>(Map.of(
                "isCritical", true,
                "suggestions", java.util.List.of(
                    "Please stay calm and listen carefully.",
                    "Ask if they are safe right now."
                ),
                "detectedEmotion", "crisis",
                "urgencyLevel", "critical",
                "alert", "⚠️ User may be in distress. Please respond with care and consider connecting them to professional help."
            ));
        }

        //  Regular suggestions via AI
        AiSuggestionService.SuggestionResult result =
                aiSuggestionService.generateSuggestions(transcript, userMood);

        return new ApiResponse<>(Map.of(
            "isCritical", false,
            "suggestions", result.suggestions,
            "detectedEmotion", result.detectedEmotion,
            "urgencyLevel", result.urgencyLevel,
            "alert", ""
        ));
    }

    // PHASE 5 — USER MEMORY / PROFILE
    
    // Get current user's memory profile
    @GetMapping("/user/memory")
    public ApiResponse<Map<String, Object>> getUserMemory() {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMemoryRepository.findByUserId(user.getId())
                .map(m -> new ApiResponse<>(Map.of(
                    "totalSessions", (Object) m.getTotalSessions(),
                    "avgSatisfactionScore", m.getAvgSatisfactionScore(),
                    "dominantEmotion", m.getDominantEmotion() != null ? m.getDominantEmotion() : "neutral",
                    "recurringTopics", m.getRecurringTopics() != null ? m.getRecurringTopics() : "",
                    "recurringStress", m.isRecurringStress(),
                    "lastSessionSentiment", m.getLastSessionSentiment() != null ? m.getLastSessionSentiment() : "neutral",
                    "emotionalTrend", m.getEmotionalTrend() != null ? m.getEmotionalTrend() : "stable",
                    "updatedAt", m.getUpdatedAt().toString()
                )))
                .orElse(new ApiResponse<>(Map.of(
                    "totalSessions", 0,
                    "avgSatisfactionScore", 5.0,
                    "dominantEmotion", "neutral",
                    "recurringTopics", "",
                    "recurringStress", false,
                    "lastSessionSentiment", "neutral",
                    "emotionalTrend", "stable",
                    "updatedAt", ""
                )));
    }

    // PHASE 4 — SESSION EVALUATION
    
    // Get evaluation for a specific session
    @GetMapping("/session/{sessionId}/evaluation")
    public ApiResponse<Map<String, Object>> getSessionEvaluation(@PathVariable UUID sessionId) {
        return evaluationRepository.findBySessionId(sessionId)
                .map(e -> new ApiResponse<>(Map.of(
                    "effectivenessScore", (Object) e.getEffectivenessScore(),
                    "durationSeconds", e.getDurationSeconds(),
                    "positiveOutcome", e.isPositiveOutcome(),
                    "flagged", e.isFlagged(),
                    "anomaly", e.isAnomaly(),
                    "engagementLevel", e.getEngagementLevel()
                )))
                .orElse(new ApiResponse<>(Map.of("message", "Not evaluated yet")));
    }
}
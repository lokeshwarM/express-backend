package com.express.expressbackend.domain.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class AiSuggestionService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.model}")
    private String model;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ Critical distress keywords — trigger immediate alert
    private static final List<String> CRITICAL_KEYWORDS = List.of(
        "suicide", "kill myself", "end my life", "want to die",
        "self harm", "hurt myself", "can't go on", "no reason to live",
        "overdose", "cutting myself"
    );

    // ✅ Check transcript for critical keywords — no AI needed, fast local check
    public boolean isCriticalState(String transcript) {
        if (transcript == null || transcript.isBlank()) return false;
        String lower = transcript.toLowerCase();
        return CRITICAL_KEYWORDS.stream().anyMatch(lower::contains);
    }

    // ✅ Generate empathetic response suggestions for listener
    public SuggestionResult generateSuggestions(String transcript, String userMood) {
        try {
            String moodContext = userMood != null ? "The user's pre-session mood was: " + userMood + "." : "";

            String prompt = """
                You are a silent AI co-pilot helping a listener on a mental wellness platform.
                A user is talking to a listener right now.
                %s
                
                Recent conversation transcript:
                "%s"
                
                Based on what the user is saying, generate 2 short, empathetic response suggestions 
                the listener can use. Keep each suggestion under 15 words.
                Also detect the user's current emotional state.
                
                Return ONLY a JSON object:
                {
                  "suggestions": ["suggestion 1", "suggestion 2"],
                  "detectedEmotion": "stressed/anxious/sad/angry/neutral/positive",
                  "urgencyLevel": "low/medium/high"
                }
                """.formatted(moodContext, transcript);

            String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", model,
                "messages", new Object[]{
                    Map.of("role", "user", "content", prompt)
                },
                "max_tokens", 200,
                "temperature", 0.7
            ));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").get(0)
                .path("message").path("content").asText();

            content = content.replaceAll("```json|```", "").trim();
            JsonNode result = objectMapper.readTree(content);

            SuggestionResult sr = new SuggestionResult();
            sr.suggestions = List.of(
                result.path("suggestions").get(0).asText("I hear you, tell me more."),
                result.path("suggestions").get(1).asText("That sounds really difficult.")
            );
            sr.detectedEmotion = result.path("detectedEmotion").asText("neutral");
            sr.urgencyLevel = result.path("urgencyLevel").asText("low");
            return sr;

        } catch (Exception e) {
            // Fallback suggestions if AI fails
            SuggestionResult fallback = new SuggestionResult();
            fallback.suggestions = List.of(
                "I hear you, please continue.",
                "That sounds really tough, I'm here."
            );
            fallback.detectedEmotion = "neutral";
            fallback.urgencyLevel = "low";
            return fallback;
        }
    }

    public static class SuggestionResult {
        public List<String> suggestions;
        public String detectedEmotion;
        public String urgencyLevel;
    }
}
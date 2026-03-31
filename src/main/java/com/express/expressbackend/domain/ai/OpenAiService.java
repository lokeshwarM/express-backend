package com.express.expressbackend.domain.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.model}")
    private String model;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SentimentResult analyzeSentiment(String reviewText) {
        try {
            String prompt = """
                Analyze this user review from a listener platform session.
                Return a JSON object with exactly these fields:
                - sentiment: "positive", "negative", or "neutral"
                - confidenceScore: a number between 0.0 and 1.0
                - keyTopics: a comma-separated string of main topics (max 5)
                - satisfactionScore: a number from 1 to 10
                
                Review: "%s"
                
                Respond with ONLY the JSON object, no explanation.
                """.formatted(reviewText);

            String requestBody = objectMapper.writeValueAsString(Map.of(
                "contents", new Object[]{
                    Map.of("parts", new Object[]{
                        Map.of("text", prompt)
                    })
                }
            ));

            // Gemini uses API key as query param
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

            content = content.replaceAll("```json|```", "").trim();

            JsonNode result = objectMapper.readTree(content);

            SentimentResult sr = new SentimentResult();
            sr.sentiment = result.path("sentiment").asText("neutral");
            sr.confidenceScore = result.path("confidenceScore").asDouble(0.5);
            sr.keyTopics = result.path("keyTopics").asText("");
            sr.satisfactionScore = result.path("satisfactionScore").asInt(5);
            return sr;

        } catch (Exception e) {
            SentimentResult fallback = new SentimentResult();
            fallback.sentiment = "neutral";
            fallback.confidenceScore = 0.5;
            fallback.keyTopics = "";
            fallback.satisfactionScore = 5;
            return fallback;
        }
    }

    public static class SentimentResult {
        public String sentiment;
        public double confidenceScore;
        public String keyTopics;
        public int satisfactionScore;
    }
}
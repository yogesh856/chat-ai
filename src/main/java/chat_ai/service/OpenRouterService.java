package chat_ai.service;





import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenRouterService {

    private final WebClient webClient;

    public OpenRouterService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://openrouter.ai/api/v1")
                .build();
    }

    // Ye final version hai — isi ko use karenge ChatService me
    public String getAIResponse(String apiKey, String model, List<Map<String, String>> conversationHistory) {

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", conversationHistory
        );

        try {
            JsonNode response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            return response
                    .get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get AI response: " + e.getMessage());
        }
    }
}
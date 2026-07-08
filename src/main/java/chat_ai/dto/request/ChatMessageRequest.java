package chat_ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ChatMessageRequest {

    private Long conversationId;      // null hoga agar naya chat start ho raha hai

    @NotBlank(message = "Message content is required")
    private String content;

    @NotBlank(message = "API key is required")
    private String apiKey;            // user ki OpenRouter API key

    @NotBlank(message = "Model is required")
    private String model;             // e.g. "anthropic/claude-3.5-sonnet"

    private List<Long> attachmentIds; // pehle se uploaded files ke IDs (Step 7 me upload karenge)
}

package chat_ai.dto.response;



import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ChatMessageResponse {
    private Long conversationId;
    private Long messageId;
    private String sender;       // "USER" ya "AI"
    private String content;
    private List<String> attachmentUrls;
    private LocalDateTime timestamp;
}

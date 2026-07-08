package chat_ai.controller;



import chat_ai.dto.request.ChatMessageRequest;
import chat_ai.dto.response.ChatMessageResponse;
import chat_ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {

        // 1. Handshake ke time WebSocketAuthInterceptor ne jo email attributes me daala tha, wo yaha milega
        String userEmail = (String) headerAccessor.getSessionAttributes().get("email");

        if (userEmail == null) {
            throw new RuntimeException("Unauthorized WebSocket session");
        }

        try {
            // 2. ChatService call karo — DB save + AI call sab yahi hoga
            ChatMessageResponse response = chatService.sendMessage(userEmail, request);

            // 3. Response ko specific user ke topic pe publish karo
            String destination = "/topic/chat/" + response.getConversationId();
            messagingTemplate.convertAndSend(destination, response);

        } catch (Exception e) {
            // 4. Error bhi ek alag error-topic pe bhej do taaki frontend handle kar sake
            String errorDestination = "/topic/chat/error/" + userEmail;
            messagingTemplate.convertAndSend(errorDestination, Map.of("error", e.getMessage()));
        }
    }
}

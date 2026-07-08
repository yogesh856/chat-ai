package chat_ai.service;



import chat_ai.dto.request.ChatMessageRequest;
import chat_ai.dto.response.ChatMessageResponse;
import chat_ai.entity.*;
import chat_ai.repository.AttachmentRepository;
import chat_ai.repository.ConversationRepository;
import chat_ai.repository.MessageRepository;
import chat_ai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final OpenRouterService openRouterService;

    public ChatMessageResponse sendMessage(String userEmail, ChatMessageRequest request) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Conversation dhundo ya naya banao
        Conversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        } else {
            conversation = new Conversation();
            conversation.setUser(user);
            conversation.setTitle(
                    request.getContent().length() > 40
                            ? request.getContent().substring(0, 40) + "..."
                            : request.getContent()
            );
            conversation = conversationRepository.save(conversation);
        }

        // 2. User ka message DB me save karo
        Message userMessage = new Message();
        userMessage.setConversation(conversation);
        userMessage.setSender(SenderType.User);
        userMessage.setContent(request.getContent());
        userMessage = messageRepository.save(userMessage);

        // 3. Agar attachments hain, unko is message se link karo
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            for (Long attachmentId : request.getAttachmentIds()) {
                Attachment attachment = attachmentRepository.findById(attachmentId)
                        .orElseThrow(() -> new RuntimeException("Attachment not found: " + attachmentId));
                attachment.setMessage(userMessage);
                attachmentRepository.save(attachment);
            }
        }

        // 4. Poori conversation history build karo (AI ko context dene ke liye)
        List<Message> allMessages = messageRepository.findByConversationOrderByCreatedAtAsc(conversation);

        List<Map<String, String>> history = allMessages.stream()
                .map(msg -> Map.of(
                        "role", msg.getSender() == SenderType.User ? "user" : "assistant",
                        "content", msg.getContent()
                ))
                .collect(Collectors.toList());

        // 5. AI se response mangwao
        String aiReplyContent = openRouterService.getAIResponse(
                request.getApiKey(),
                request.getModel(),
                history
        );

        // 6. AI ka response bhi DB me save karo
        Message aiMessage = new Message();
        aiMessage.setConversation(conversation);
        aiMessage.setSender(SenderType.AI);
        aiMessage.setContent(aiReplyContent);
        aiMessage = messageRepository.save(aiMessage);

        // 7. Response DTO banao aur return karo
        return new ChatMessageResponse(
                conversation.getId(),
                aiMessage.getId(),
                "AI",
                aiReplyContent,
                new ArrayList<>(),
                aiMessage.getCreatedAt()
        );
    }

    public List<Conversation> getUserConversations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return conversationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Message> getConversationMessages(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
    }
}
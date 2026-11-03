package org.example.springaiproject.chat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatService {

    private static final int MAX_CONTENT_LENGTH = 4000;
    private static final int STORED_MESSAGES = 100;
    private static final int CONTEXT_MESSAGES = 20;

    private final ChatRepository repository;
    private final ChatGateway gateway;

    public ChatService(ChatRepository repository, ChatGateway gateway) {
        this.repository = repository;
        this.gateway = gateway;
    }

    public ChatInfo createChat() {
        return repository.create();
    }

    public List<ChatMessage> getMessages(UUID chatId) {
        return repository.find(chatId).messages;
    }

    public ChatMessage sendMessage(UUID chatId, String content) {
        String input = validate(content);
        ChatRepository.ChatSession session = repository.find(chatId);

        // Serialize turns within one chat; other chats can call the model independently.
        synchronized (session) {
            List<ChatMessage> history = session.messages;
            List<ChatMessage> context = List.copyOf(
                    history.subList(Math.max(0, history.size() - CONTEXT_MESSAGES), history.size()));
            ChatMessage question = new ChatMessage(UUID.randomUUID(), ChatMessage.Role.USER, input, Instant.now());
            String answer = gateway.reply(context, input);
            if (answer == null || answer.isBlank()) {
                throw new ChatException(ChatException.Reason.MODEL_UNAVAILABLE,
                        "The model returned an empty response. Try sending your message again.");
            }
            ChatMessage reply = new ChatMessage(UUID.randomUUID(), ChatMessage.Role.ASSISTANT, answer, Instant.now());
            List<ChatMessage> updated = new ArrayList<>(history);
            updated.add(question);
            updated.add(reply);
            session.messages = List.copyOf(
                    updated.subList(Math.max(0, updated.size() - STORED_MESSAGES), updated.size()));
            return reply;
        }
    }

    private String validate(String content) {
        if (content == null || content.isBlank() || content.length() > MAX_CONTENT_LENGTH) {
            throw new ChatException(ChatException.Reason.INVALID_MESSAGE,
                    "The message must contain between 1 and 4000 characters and cannot consist only of whitespace.");
        }
        return content.strip();
    }
}

package org.example.springaiproject.chat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRepository {

    private static final int MAX_CHATS = 100;
    private final Map<UUID, ChatSession> chats = new ConcurrentHashMap<>();

    public synchronized ChatInfo create() {
        if (chats.size() >= MAX_CHATS) {
            throw new ChatException(ChatException.Reason.CAPACITY_REACHED,
                    "The limit of 100 chats has been reached. Restart the application to start over.");
        }
        ChatInfo info = new ChatInfo(UUID.randomUUID(), Instant.now());
        chats.put(info.id(), new ChatSession());
        return info;
    }

    ChatSession find(UUID id) {
        ChatSession session = chats.get(id);
        if (session == null) {
            throw new ChatException(ChatException.Reason.CHAT_NOT_FOUND,
                    "Chat not found. Create a new chat.");
        }
        return session;
    }

    static final class ChatSession {

        // A complete, immutable snapshot also lets readers see the previous turn while a model is busy.
        volatile List<ChatMessage> messages = List.of();
    }
}

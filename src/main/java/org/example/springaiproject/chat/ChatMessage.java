package org.example.springaiproject.chat;

import java.time.Instant;
import java.util.UUID;

public record ChatMessage(UUID id, Role role, String content, Instant createdAt) {

    public enum Role {
        USER, ASSISTANT
    }
}

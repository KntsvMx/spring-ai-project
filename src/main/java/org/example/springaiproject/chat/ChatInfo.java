package org.example.springaiproject.chat;

import java.time.Instant;
import java.util.UUID;

public record ChatInfo(UUID id, Instant createdAt) {
}

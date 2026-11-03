package org.example.springaiproject.chat;

import java.util.List;

@FunctionalInterface
public interface ChatGateway {

    String reply(List<ChatMessage> history, String content);
}

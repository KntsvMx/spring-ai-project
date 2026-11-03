package org.example.springaiproject.web;

import org.example.springaiproject.chat.ChatInfo;
import org.example.springaiproject.chat.ChatMessage;
import org.example.springaiproject.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ChatInfo> createChat() {
        ChatInfo chat = service.createChat();
        return ResponseEntity.created(URI.create("/api/chats/" + chat.id() + "/chatMessages")).body(chat);
    }

    @GetMapping("/{chatId}/chatMessages")
    public List<ChatMessage> getMessages(@PathVariable("chatId") UUID chatId) {
        return service.getMessages(chatId);
    }

    @PostMapping("/{chatId}/chatMessages")
    public ChatMessage sendMessage(@PathVariable("chatId") UUID chatId,
                                   @RequestBody(required = false) MessageRequest request) {
        return service.sendMessage(chatId, request == null ? null : request.content());
    }

    public record MessageRequest(String content) {
    }
}

package org.example.springaiproject.ollama;

import org.example.springaiproject.chat.ChatException;
import org.example.springaiproject.chat.ChatGateway;
import org.example.springaiproject.chat.ChatMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

public class OllamaChatGateway implements ChatGateway {

    private final ChatClient chatClient;

    public OllamaChatGateway(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String reply(List<ChatMessage> history, String content) {
        List<Message> messages = new ArrayList<>();
        for (ChatMessage message : history) {
            messages.add(switch (message.role()) {
                case USER -> new UserMessage(message.content());
                case ASSISTANT -> new AssistantMessage(message.content());
            });
        }
        // Message objects keep braces in Java/JSON examples literal instead of interpreting a template.
        messages.add(new UserMessage(content));
        try {
            return chatClient.prompt().messages(messages).call().content();
        } catch (RuntimeException exception) {
            throw new ChatException(ChatException.Reason.MODEL_UNAVAILABLE,
                    "Could not get a response from Ollama. Check that Ollama is running "
                            + "and the model specified by OLLAMA_MODEL is downloaded.", exception);
        }
    }
}

package org.example.springaiproject.config;

import org.example.springaiproject.chat.ChatGateway;
import org.example.springaiproject.chat.ChatRepository;
import org.example.springaiproject.chat.ChatService;
import org.example.springaiproject.ollama.OllamaChatGateway;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class ChatConfiguration {

    @Bean
    RestClient.Builder ollamaRestClientBuilder(
            @Value("${spring.http.clients.connect-timeout}") Duration connectTimeout,
            @Value("${spring.http.clients.read-timeout}") Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);
        return RestClient.builder().requestFactory(factory);
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, @Value("${chatbot.system-prompt}") String systemPrompt) {
        return builder.defaultSystem(systemPrompt).build();
    }

    @Bean
    ChatGateway chatGateway(ChatClient chatClient) {
        return new OllamaChatGateway(chatClient);
    }

    @Bean
    ChatRepository chatRepository() {
        return new ChatRepository();
    }

    @Bean
    ChatService chatService(ChatRepository repository, ChatGateway gateway) {
        return new ChatService(repository, gateway);
    }
}

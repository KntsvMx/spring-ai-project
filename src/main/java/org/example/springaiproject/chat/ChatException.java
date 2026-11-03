package org.example.springaiproject.chat;

public class ChatException extends RuntimeException {

    private final Reason reason;

    public ChatException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public ChatException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public enum Reason {
        INVALID_MESSAGE, CHAT_NOT_FOUND, CAPACITY_REACHED, MODEL_UNAVAILABLE
    }
}

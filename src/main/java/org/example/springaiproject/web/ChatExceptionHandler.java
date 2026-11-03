package org.example.springaiproject.web;

import org.example.springaiproject.chat.ChatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ChatExceptionHandler {

    @ExceptionHandler(ChatException.class)
    public ProblemDetail handleChatException(ChatException exception) {
        HttpStatus status = switch (exception.reason()) {
            case INVALID_MESSAGE -> HttpStatus.BAD_REQUEST;
            case CHAT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CAPACITY_REACHED -> HttpStatus.TOO_MANY_REQUESTS;
            case MODEL_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
        };
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problem.setProperty("code", exception.reason().name());
        return problem;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleMalformedRequest(Exception exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Check the request JSON and make sure the chat ID is a valid UUID.");
        problem.setProperty("code", "INVALID_REQUEST");
        return problem;
    }
}

package com.xxxx.votting_spring.exception;

public class ContentViolationException extends RuntimeException {
    public ContentViolationException(String message) {
        super(message);
    }
}

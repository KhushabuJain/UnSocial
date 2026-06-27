package com.unsocial.unsocial.exception;

/**
 * Thrown when the local Ollama server can't be reached, or returns an
 * unusable response (e.g. the configured model hasn't been pulled).
 */
public class OllamaUnavailableException extends RuntimeException {
    public OllamaUnavailableException(String message) {
        super(message);
    }
}

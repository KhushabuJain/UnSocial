package com.unsocial.unsocial.service;

import com.unsocial.unsocial.exception.OllamaUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Thin client for the local Ollama server (https://ollama.com) — handles
 * both chat completions and embeddings. No API key needed since Ollama
 * runs entirely on the developer's own machine.
 *
 * Uses Ollama's modern endpoints:
 *   POST /api/embed  — { model, input } -> { embeddings: [[...]] }
 *   POST /api/chat   — { model, messages, stream } -> { message: { role, content } }
 */
@Slf4j
@Service
public class OllamaClient {

    private final RestClient restClient;
    private final String baseUrl;
    private final String chatModel;
    private final String embeddingModel;

    public OllamaClient(
            RestClient.Builder builder,
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.chat-model:llama3.2}") String chatModel,
            @Value("${ollama.embedding-model:nomic-embed-text}") String embeddingModel
    ) {
        this.baseUrl = baseUrl;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    /** Embeds a single piece of text and returns its vector. */
    public float[] embed(String text) {
        try {
            EmbedResponse response = restClient.post()
                    .uri("/api/embed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new EmbedRequest(embeddingModel, text))
                    .retrieve()
                    .body(EmbedResponse.class);

            if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
                throw new OllamaUnavailableException(
                        "Ollama returned no embedding. Is the '" + embeddingModel +
                                "' model pulled? Run: ollama pull " + embeddingModel);
            }
            List<Double> vec = response.embeddings().get(0);
            float[] arr = new float[vec.size()];
            for (int i = 0; i < vec.size(); i++) arr[i] = vec.get(i).floatValue();
            return arr;

        } catch (OllamaUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to reach Ollama embeddings endpoint: {}", e.getMessage());
            throw new OllamaUnavailableException(
                    "Couldn't reach Ollama at " + baseUrl + ". Make sure it's running (`ollama serve`) " +
                            "and that you've pulled '" + embeddingModel + "' (`ollama pull " + embeddingModel + "`).");
        }
    }

    /** Sends a chat completion request and returns the assistant's reply text. */
    public String chat(List<OllamaMessage> messages) {
        try {
            ChatResponse response = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ChatRequest(chatModel, messages, false))
                    .retrieve()
                    .body(ChatResponse.class);

            if (response == null || response.message() == null || response.message().content() == null) {
                throw new OllamaUnavailableException(
                        "Ollama returned an empty reply. Is the '" + chatModel +
                                "' model pulled? Run: ollama pull " + chatModel);
            }
            return response.message().content();

        } catch (OllamaUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to reach Ollama chat endpoint: {}", e.getMessage());
            throw new OllamaUnavailableException(
                    "Couldn't reach Ollama at " + baseUrl + ". Make sure it's running (`ollama serve`) " +
                            "and that you've pulled '" + chatModel + "' (`ollama pull " + chatModel + "`).");
        }
    }

    // ──────────────────────────────────────────────
    // Ollama wire format
    // ──────────────────────────────────────────────

    public record OllamaMessage(String role, String content) {}

    private record EmbedRequest(String model, String input) {}
    private record EmbedResponse(List<List<Double>> embeddings) {}

    private record ChatRequest(String model, List<OllamaMessage> messages, boolean stream) {}
    private record ChatResponse(String model, OllamaMessage message, boolean done) {}
}

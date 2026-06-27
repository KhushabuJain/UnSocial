package com.unsocial.unsocial.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helpers for storing embedding vectors as JSON text in MySQL (which has
 * no native vector column type here) and comparing them with cosine
 * similarity — a simple, dependency-free substitute for a real vector DB,
 * appropriate for a knowledge base of a few hundred chunks at most.
 */
public final class EmbeddingUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private EmbeddingUtil() {}

    public static String toJson(float[] embedding) {
        try {
            return MAPPER.writeValueAsString(embedding);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize embedding", e);
        }
    }

    public static float[] fromJson(String json) {
        try {
            return MAPPER.readValue(json, float[].class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize embedding", e);
        }
    }

    /** Returns a value in [-1, 1] — higher means more semantically similar. */
    public static double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length || a.length == 0) return -1;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}

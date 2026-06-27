package com.unsocial.unsocial.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a knowledge-base markdown file into retrievable chunks.
 *
 * Strategy: split at "## " (H2) boundaries first — each section in our
 * knowledge base markdown is a self-contained topic. If a section is
 * still too long for a good embedding, it's further split by paragraph
 * so no single chunk overwhelms the embedding model or dilutes its vector.
 */
public final class MarkdownChunker {

    private static final int MAX_CHUNK_CHARS = 1100;

    private MarkdownChunker() {}

    public record Chunk(String sectionTitle, String content) {}

    public static List<Chunk> chunk(String markdown) {
        List<Chunk> chunks = new ArrayList<>();
        String[] sections = markdown.split("(?m)^## ");

        for (String raw : sections) {
            if (raw == null || raw.isBlank()) continue;

            String[] lines = raw.split("\n", 2);
            String title = lines[0].trim();
            String body = lines.length > 1 ? lines[1].trim() : "";

            if (title.isEmpty() && body.isEmpty()) continue;
            if (body.isEmpty()) body = title; // first chunk before any "## " (the H1 + intro)

            if (body.length() <= MAX_CHUNK_CHARS) {
                chunks.add(new Chunk(title, title + "\n" + body));
                continue;
            }

            // Section too long — split into paragraph groups under the size cap
            String[] paragraphs = body.split("\n\n+");
            StringBuilder buffer = new StringBuilder();
            for (String p : paragraphs) {
                if (!buffer.isEmpty() && buffer.length() + p.length() > MAX_CHUNK_CHARS) {
                    chunks.add(new Chunk(title, title + "\n" + buffer.toString().trim()));
                    buffer.setLength(0);
                }
                buffer.append(p).append("\n\n");
            }
            if (!buffer.isEmpty()) {
                chunks.add(new Chunk(title, title + "\n" + buffer.toString().trim()));
            }
        }
        return chunks;
    }
}

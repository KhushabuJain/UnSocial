package com.unsocial.unsocial.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * One retrievable piece of the chatbot's knowledge base — a chunk of
 * markdown content from resources/knowledge-base/, along with its
 * embedding vector (stored as a JSON-serialized float array, since the
 * app's MySQL instance has no native vector column type). Similarity
 * search is done in Java via cosine similarity — see EmbeddingUtil.
 */
@Entity
@Table(name = "knowledge_chunks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // JSON array of floats, e.g. "[0.123,-0.045,...]"
    @Column(columnDefinition = "TEXT", nullable = false)
    private String embedding;

    // Which knowledge-base file this came from, e.g. "app-help.md"
    @Column(nullable = false)
    private String source;

    // The "## heading" this chunk was extracted from
    @Column(name = "section_title")
    private String sectionTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KnowledgeCategory category;

    @Column(name = "chunk_index")
    private int chunkIndex;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

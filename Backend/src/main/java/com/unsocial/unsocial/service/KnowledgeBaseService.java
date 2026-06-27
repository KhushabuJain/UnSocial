package com.unsocial.unsocial.service;

import com.unsocial.unsocial.entity.KnowledgeCategory;
import com.unsocial.unsocial.entity.KnowledgeChunk;
import com.unsocial.unsocial.repository.KnowledgeChunkRepository;
import com.unsocial.unsocial.util.EmbeddingUtil;
import com.unsocial.unsocial.util.MarkdownChunker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Owns the chatbot's knowledge base: reading the markdown source files,
 * chunking + embedding them into KnowledgeChunk rows (reindex), and
 * retrieving the most relevant chunks for a given query (retrieveTopK) —
 * the "R" in RAG.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final OllamaClient ollamaClient;

    private static final String KB_LOCATION = "classpath:knowledge-base/*.md";

    /** Wipes and rebuilds the entire knowledge base from the markdown files
     *  in resources/knowledge-base/. Requires Ollama to be running. */
    @Transactional
    public int reindex() {
        log.info("📚 Re-indexing knowledge base…");
        knowledgeChunkRepository.deleteAllInBatch();

        List<KnowledgeChunk> toSave = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(KB_LOCATION);

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                KnowledgeCategory category = filename != null && filename.startsWith("app-help")
                        ? KnowledgeCategory.APP_HELP
                        : KnowledgeCategory.SAFETY;

                String markdown = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                List<MarkdownChunker.Chunk> chunks = MarkdownChunker.chunk(markdown);

                int idx = 0;
                for (MarkdownChunker.Chunk chunk : chunks) {
                    float[] embedding = ollamaClient.embed(chunk.content());
                    toSave.add(KnowledgeChunk.builder()
                            .content(chunk.content())
                            .embedding(EmbeddingUtil.toJson(embedding))
                            .source(filename)
                            .sectionTitle(chunk.sectionTitle())
                            .category(category)
                            .chunkIndex(idx++)
                            .build());
                }
                log.info("   {} → {} chunk(s)", filename, idx);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read knowledge base files", e);
        }

        knowledgeChunkRepository.saveAll(toSave);
        log.info("✅ Knowledge base indexed: {} chunk(s) total", toSave.size());
        return toSave.size();
    }

    /** Returns the top-K most relevant chunks for a query, ranked by cosine similarity. */
    public List<KnowledgeChunk> retrieveTopK(String query, int k) {
        float[] queryEmbedding = ollamaClient.embed(query);
        List<KnowledgeChunk> all = knowledgeChunkRepository.findAll();

        return all.stream()
                .map(c -> Map.entry(c, EmbeddingUtil.cosineSimilarity(queryEmbedding, EmbeddingUtil.fromJson(c.getEmbedding()))))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(k)
                .map(Map.Entry::getKey)
                .toList();
    }

    public long chunkCount() {
        return knowledgeChunkRepository.count();
    }
}

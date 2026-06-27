package com.unsocial.unsocial.config;

import com.unsocial.unsocial.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Auto-indexes the chatbot's knowledge base on startup, but only if it's
 * empty — so restarts don't re-embed everything every time. Wrapped in a
 * try/catch so a missing/unstarted Ollama instance never prevents the
 * rest of the app from booting; just start Ollama and call
 * POST /api/chatbot/admin/reindex (ADMIN role) when ready.
 */
@Slf4j
@Component
@Order(Integer.MAX_VALUE)
@RequiredArgsConstructor
public class KnowledgeBaseStartupRunner implements ApplicationRunner {

    private final KnowledgeBaseService knowledgeBaseService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            long existing = knowledgeBaseService.chunkCount();
            if (existing == 0) {
                log.info("🤖 Knowledge base is empty — indexing on startup (requires Ollama to be running)…");
                knowledgeBaseService.reindex();
            } else {
                log.info("🤖 Knowledge base already indexed ({} chunks) — skipping auto re-index. " +
                                "Call POST /api/chatbot/admin/reindex after editing knowledge-base content.",
                        existing);
            }
        } catch (Exception e) {
            log.warn("⚠️  Couldn't auto-index the knowledge base on startup (is Ollama running?): {}. " +
                            "The app will still start normally — start Ollama and call " +
                            "POST /api/chatbot/admin/reindex when ready.",
                    e.getMessage());
        }
    }
}

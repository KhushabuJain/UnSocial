package com.unsocial.unsocial.service;

import com.unsocial.unsocial.dto.ChatAnswerResponse;
import com.unsocial.unsocial.dto.ChatMessageResponse;
import com.unsocial.unsocial.entity.ChatMessage;
import com.unsocial.unsocial.entity.ChatRole;
import com.unsocial.unsocial.entity.KnowledgeChunk;
import com.unsocial.unsocial.entity.User;
import com.unsocial.unsocial.repository.ChatMessageRepository;
import com.unsocial.unsocial.service.OllamaClient.OllamaMessage;
import com.unsocial.unsocial.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates the full RAG pipeline for the AI Safety Assistant:
 *   1. retrieve relevant knowledge-base chunks for the question (KnowledgeBaseService)
 *   2. build a grounded prompt with those chunks + recent conversation history
 *   3. ask the local LLM via Ollama (OllamaClient)
 *   4. persist both turns so history survives across sessions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatMessageRepository chatMessageRepository;
    private final KnowledgeBaseService knowledgeBaseService;
    private final OllamaClient ollamaClient;
    private final SecurityUtils securityUtils;

    @Value("${chatbot.top-k:4}")
    private int topK;

    @Value("${chatbot.history-context-size:6}")
    private int historyContextSize;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are the UnSocial Safety Assistant — a helpful in-app guide for the UnSocial \
            personal safety app. You help with two kinds of questions: (1) general personal \
            safety guidance, and (2) how to use UnSocial's own features (SOS Alert, Fake Call, \
            Fake Message, Live Tracking, Safety Timer, Emergency Contacts).

            Ground your answers in the CONTEXT below, which comes from UnSocial's own knowledge \
            base. If the context doesn't cover something, say so honestly instead of guessing, \
            and suggest a reliable next step.

            Rules you must always follow:
            - If the user describes being in immediate danger or an active emergency, your first \
            priority is to tell them to contact local emergency services right now (in India: \
            112) and to use UnSocial's SOS Alert feature — say this before anything else.
            - You are not a substitute for the police, a lawyer, a doctor, or a mental health \
            professional. For legal, medical, or psychological matters, give general information \
            only and recommend a qualified professional or official helpline for specifics.
            - Be warm, direct, and practical. Keep answers concise unless the user asks for more detail.
            - Never invent emergency numbers, legal facts, or app behavior that isn't in the context.

            CONTEXT:
            %s
            """;

    @Transactional
    public ChatAnswerResponse ask(String userMessage) {
        User user = securityUtils.getCurrentUser();

        // 1. Pull recent conversation history BEFORE adding the new message,
        //    so we don't have to de-duplicate it back out below.
        List<ChatMessage> recent = chatMessageRepository
                .findTop10ByUserIdOrderByCreatedAtDesc(user.getId());
        Collections.reverse(recent);
        int from = Math.max(0, recent.size() - historyContextSize);
        List<ChatMessage> recentTrimmed = recent.subList(from, recent.size());

        // 2. Save the user's new message
        chatMessageRepository.save(ChatMessage.builder()
                .user(user).role(ChatRole.USER).content(userMessage).build());

        // 3. Retrieve relevant knowledge (the "R" in RAG)
        List<KnowledgeChunk> relevant = knowledgeBaseService.retrieveTopK(userMessage, topK);
        String context = relevant.isEmpty()
                ? "(no matching knowledge base content found)"
                : relevant.stream()
                    .map(c -> "- " + c.getContent())
                    .collect(Collectors.joining("\n\n"));

        // 4. Build the message list: system (with context) + recent history + new question
        List<OllamaMessage> messages = new ArrayList<>();
        messages.add(new OllamaMessage("system", String.format(SYSTEM_PROMPT_TEMPLATE, context)));
        for (ChatMessage m : recentTrimmed) {
            messages.add(new OllamaMessage(
                    m.getRole() == ChatRole.USER ? "user" : "assistant",
                    m.getContent()
            ));
        }
        messages.add(new OllamaMessage("user", userMessage));

        // 5. Call the local LLM
        String reply = ollamaClient.chat(messages);

        // 6. Save assistant reply
        chatMessageRepository.save(ChatMessage.builder()
                .user(user).role(ChatRole.ASSISTANT).content(reply).build());

        List<String> sources = relevant.stream()
                .map(KnowledgeChunk::getSource)
                .distinct()
                .toList();

        return ChatAnswerResponse.builder()
                .reply(reply)
                .sources(sources)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public List<ChatMessageResponse> getHistory() {
        Long userId = securityUtils.getCurrentUserId();
        return chatMessageRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(m -> ChatMessageResponse.builder()
                        .id(m.getId())
                        .role(m.getRole().name())
                        .content(m.getContent())
                        .createdAt(m.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void clearHistory() {
        Long userId = securityUtils.getCurrentUserId();
        chatMessageRepository.deleteByUserId(userId);
    }
}

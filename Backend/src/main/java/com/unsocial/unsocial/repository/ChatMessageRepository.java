package com.unsocial.unsocial.repository;

import com.unsocial.unsocial.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUserIdOrderByCreatedAtAsc(Long userId);

    // Used to build a short conversational-context window for the LLM call
    List<ChatMessage> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}

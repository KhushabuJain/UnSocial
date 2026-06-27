package com.unsocial.unsocial.repository;

import com.unsocial.unsocial.entity.KnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {
    // findAll(), count(), deleteAllInBatch() inherited — the knowledge base
    // is small enough (tens to low hundreds of chunks) that loading it
    // entirely into memory for similarity search is fast and simple.
}

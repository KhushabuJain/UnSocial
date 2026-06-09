package com.unsocial.unsocial.repository;

import com.unsocial.unsocial.entity.FakeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FakeMessageRepository extends JpaRepository<FakeMessage, Long> {

    List<FakeMessage> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<FakeMessage> findByIdAndUserId(Long id, Long userId);

    Optional<FakeMessage> findByUserIdAndIsDefaultTrue(Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Query("UPDATE FakeMessage f SET f.isDefault = false WHERE f.user.id = :userId")
    void clearDefaultForUser(Long userId);
}

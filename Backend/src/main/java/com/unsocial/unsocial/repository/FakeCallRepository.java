package com.unsocial.unsocial.repository;

import com.unsocial.unsocial.entity.FakeCallTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FakeCallRepository extends JpaRepository<FakeCallTemplate, Long> {

    List<FakeCallTemplate> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<FakeCallTemplate> findByIdAndUserId(Long id, Long userId);

    Optional<FakeCallTemplate> findByUserIdAndIsDefaultTrue(Long userId);

    long countByUserId(Long userId);

    // Unset all defaults for a user before setting a new one
    @Modifying
    @Query("UPDATE FakeCallTemplate f SET f.isDefault = false WHERE f.user.id = :userId")
    void clearDefaultForUser(Long userId);
}

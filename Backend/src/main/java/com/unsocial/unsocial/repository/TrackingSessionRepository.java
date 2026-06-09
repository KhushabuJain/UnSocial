package com.unsocial.unsocial.repository;

import com.unsocial.unsocial.entity.TrackingSession;
import com.unsocial.unsocial.entity.TrackingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingSessionRepository extends JpaRepository<TrackingSession, Long> {

    List<TrackingSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<TrackingSession> findByUserIdAndStatus(Long userId, TrackingStatus status);

    Optional<TrackingSession> findByIdAndUserId(Long id, Long userId);

    // Public endpoint — no userId check needed (shareToken is the secret)
    Optional<TrackingSession> findByShareToken(String shareToken);
}

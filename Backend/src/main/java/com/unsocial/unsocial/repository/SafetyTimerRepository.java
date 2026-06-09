package com.unsocial.unsocial.repository;

import com.unsocial.unsocial.entity.SafetyTimer;
import com.unsocial.unsocial.entity.TimerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SafetyTimerRepository extends JpaRepository<SafetyTimer, Long> {

    List<SafetyTimer> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<SafetyTimer> findByUserIdAndStatus(Long userId, TimerStatus status);

    Optional<SafetyTimer> findByIdAndUserId(Long id, Long userId);

    // Used by the scheduler to find timers that have silently expired
    List<SafetyTimer> findByStatusAndExpiresAtBefore(TimerStatus status, LocalDateTime now);
}

package com.unsocial.unsocial.service;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.entity.*;
import com.unsocial.unsocial.exception.ResourceNotFoundException;
import com.unsocial.unsocial.repository.SafetyTimerRepository;
import com.unsocial.unsocial.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyTimerService {

    private final SafetyTimerRepository safetyTimerRepository;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    // ──────────────────────────────────────────────
    // Start Timer
    // ──────────────────────────────────────────────

    @Transactional
    public SafetyTimerResponse startTimer(SafetyTimerRequest request) {
        User user = securityUtils.getCurrentUser();

        // Only one active timer allowed at a time
        safetyTimerRepository.findByUserIdAndStatus(user.getId(), TimerStatus.ACTIVE)
                .ifPresent(t -> {
                    throw new IllegalStateException(
                            "You already have an active safety timer (ID: " + t.getId() +
                                    "). Check in or cancel it before starting a new one."
                    );
                });

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(request.getDurationMinutes());

        SafetyTimer timer = SafetyTimer.builder()
                .user(user)
                .durationMinutes(request.getDurationMinutes())
                .note(request.getNote())
                .status(TimerStatus.ACTIVE)
                .expiresAt(expiresAt)
                .build();

        safetyTimerRepository.save(timer);
        log.info("Safety timer started for user {} — expires at {}", user.getEmail(), expiresAt);
        return toResponse(timer);
    }

    // ──────────────────────────────────────────────
    // Check In (user is safe)
    // ──────────────────────────────────────────────

    @Transactional
    public SafetyTimerResponse checkIn(Long timerId) {
        Long userId = securityUtils.getCurrentUserId();
        SafetyTimer timer = findOwned(timerId, userId);

        if (timer.getStatus() != TimerStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE timers can be checked in");
        }

        timer.setStatus(TimerStatus.COMPLETED);
        timer.setCompletedAt(LocalDateTime.now());
        safetyTimerRepository.save(timer);

        log.info("✅ Safety timer {} checked in — user {} is safe", timerId, userId);
        return toResponse(timer);
    }

    // ──────────────────────────────────────────────
    // Cancel Timer
    // ──────────────────────────────────────────────

    @Transactional
    public SafetyTimerResponse cancelTimer(Long timerId) {
        Long userId = securityUtils.getCurrentUserId();
        SafetyTimer timer = findOwned(timerId, userId);

        if (timer.getStatus() != TimerStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE timers can be cancelled");
        }

        timer.setStatus(TimerStatus.CANCELLED);
        timer.setCompletedAt(LocalDateTime.now());
        safetyTimerRepository.save(timer);

        log.info("Safety timer {} cancelled by user {}", timerId, userId);
        return toResponse(timer);
    }

    // ──────────────────────────────────────────────
    // Read
    // ──────────────────────────────────────────────

    public SafetyTimerResponse getActiveTimer() {
        Long userId = securityUtils.getCurrentUserId();
        SafetyTimer timer = safetyTimerRepository
                .findByUserIdAndStatus(userId, TimerStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active safety timer found"));
        return toResponse(timer);
    }

    public List<SafetyTimerResponse> getTimerHistory() {
        Long userId = securityUtils.getCurrentUserId();
        return safetyTimerRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    // Scheduler — runs every 30 seconds
    // Finds ACTIVE timers that have passed their expiresAt and marks them EXPIRED.
    // Sends emergency notification to contacts.
    // ──────────────────────────────────────────────

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void processExpiredTimers() {
        List<SafetyTimer> expired = safetyTimerRepository
                .findByStatusAndExpiresAtBefore(TimerStatus.ACTIVE, LocalDateTime.now());

        if (expired.isEmpty()) return;

        log.warn("⏰ Found {} expired safety timer(s) — triggering alerts", expired.size());

        for (SafetyTimer timer : expired) {
            timer.setStatus(TimerStatus.EXPIRED);
            timer.setCompletedAt(LocalDateTime.now());
            safetyTimerRepository.save(timer);

            User user = timer.getUser();

            log.warn("🚨 Safety timer EXPIRED for user {} — sending alerts", user.getEmail());

            // Notify contacts that the timer expired without check-in
            notificationService.notifySosTriggered(
                    user,
                    buildFakeSosForTimer(timer, user),
                    0  // TODO: inject EmergencyContactRepository and count contacts
            );

            // TODO: Optionally auto-trigger a SOS alert by calling SosService.triggerSos()
            //       Be careful to avoid circular dependency — use ApplicationContext or events.
        }
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private SafetyTimer findOwned(Long id, Long userId) {
        return safetyTimerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Safety timer not found with id: " + id));
    }

    private SafetyTimerResponse toResponse(SafetyTimer t) {
        long remaining = t.getStatus() == TimerStatus.ACTIVE
                ? Math.max(0, ChronoUnit.SECONDS.between(LocalDateTime.now(), t.getExpiresAt()))
                : 0;

        return SafetyTimerResponse.builder()
                .id(t.getId())
                .durationMinutes(t.getDurationMinutes())
                .note(t.getNote())
                .status(t.getStatus())
                .expiresAt(t.getExpiresAt())
                .remainingSeconds(remaining)
                .startedAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build();
    }

    /** Builds a minimal SosAlert object just for passing to NotificationService */
    private com.unsocial.unsocial.entity.SosAlert buildFakeSosForTimer(SafetyTimer timer, User user) {
        return com.unsocial.unsocial.entity.SosAlert.builder()
                .user(user)
                .latitude(0.0)
                .longitude(0.0)
                .message("⏰ Safety timer expired without check-in! " +
                        (timer.getNote() != null ? "Note: " + timer.getNote() : ""))
                .build();
    }
}

package com.unsocial.unsocial.service;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.entity.*;
import com.unsocial.unsocial.exception.ResourceNotFoundException;
import com.unsocial.unsocial.repository.EmergencyContactRepository;
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

    private final SafetyTimerRepository       safetyTimerRepository;
    private final EmergencyContactRepository  contactRepository;
    private final NotificationService         notificationService;
    private final SecurityUtils               securityUtils;

    // ──────────────────────────────────────────────
    // Start Timer
    // ──────────────────────────────────────────────

    @Transactional
    public SafetyTimerResponse startTimer(SafetyTimerRequest request) {
        User user = securityUtils.getCurrentUser();

        safetyTimerRepository.findByUserIdAndStatus(user.getId(), TimerStatus.ACTIVE)
                .ifPresent(t -> {
                    throw new IllegalStateException(
                            "You already have an active safety timer (ID: " + t.getId() + ").");
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
        log.info("⏱️ Safety timer started for {} — expires at {}", user.getEmail(), expiresAt);
        return toResponse(timer);
    }

    // ──────────────────────────────────────────────
    // Check In (safe before expiry)
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
    // Cancel
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
        return toResponse(timer);
    }

    // ──────────────────────────────────────────────
    // Read
    // ──────────────────────────────────────────────

    public SafetyTimerResponse getActiveTimer() {
        Long userId = securityUtils.getCurrentUserId();
        return toResponse(
                safetyTimerRepository.findByUserIdAndStatus(userId, TimerStatus.ACTIVE)
                        .orElseThrow(() -> new ResourceNotFoundException("No active safety timer found"))
        );
    }

    public List<SafetyTimerResponse> getTimerHistory() {
        Long userId = securityUtils.getCurrentUserId();
        return safetyTimerRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    // Scheduler — fires every 30 seconds
    // Finds expired ACTIVE timers → notifies contacts via email + SMS
    // ──────────────────────────────────────────────

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void processExpiredTimers() {
        List<SafetyTimer> expired = safetyTimerRepository
                .findByStatusAndExpiresAtBefore(TimerStatus.ACTIVE, LocalDateTime.now());

        if (expired.isEmpty()) return;

        log.warn("⏰ Found {} expired safety timer(s)", expired.size());

        for (SafetyTimer timer : expired) {
            timer.setStatus(TimerStatus.EXPIRED);
            timer.setCompletedAt(LocalDateTime.now());
            safetyTimerRepository.save(timer);

            User user = timer.getUser();

            // Fetch emergency contacts and send REAL notifications
            List<NotificationService.ContactInfo> contacts = contactRepository
                    .findByUserId(user.getId())
                    .stream()
                    .map(c -> new NotificationService.ContactInfo(c.getName(), c.getPhone(), c.getEmail()))
                    .collect(Collectors.toList());

            notificationService.notifyTimerExpired(user, timer.getNote(), contacts);

            log.warn("🚨 Timer expired for {} — {} contact(s) notified", user.getEmail(), contacts.size());
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
}

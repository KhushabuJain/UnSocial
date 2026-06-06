package com.unsocial.unsocial.service;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.entity.*;
import com.unsocial.unsocial.exception.ResourceNotFoundException;
import com.unsocial.unsocial.repository.SosAlertRepository;
import com.unsocial.unsocial.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SosService {

    private final SosAlertRepository sosAlertRepository;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    // ──────────────────────────────────────────────
    // Trigger SOS
    // ──────────────────────────────────────────────

    @Transactional
    public SosAlertResponse triggerSos(SosAlertRequest request) {
        User user = securityUtils.getCurrentUser();

        // Only one active SOS allowed at a time
        sosAlertRepository.findByUserIdAndStatus(user.getId(), SosStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "You already have an active SOS alert (ID: " + existing.getId() +
                                    "). Please resolve or cancel it before triggering a new one."
                    );
                });

        SosAlert alert = SosAlert.builder()
                .user(user)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .message(request.getMessage())
                .status(SosStatus.ACTIVE)
                .build();

        sosAlertRepository.save(alert);

        // ── Notify emergency contacts ──
        // TODO: Replace hardcoded count with actual contact lookup from EmergencyContact module:
        //   int count = emergencyContactRepository.countByUserId(user.getId());
        //   notificationService.notifySosTriggered(user, alert, count);
        int contactsNotified = 0; // update once EmergencyContact repo is injected
        notificationService.notifySosTriggered(user, alert, contactsNotified);

        alert.setContactsNotified(contactsNotified);
        sosAlertRepository.save(alert);

        log.warn("🚨 SOS triggered — user: {}, alertId: {}", user.getEmail(), alert.getId());
        return toResponse(alert);
    }

    // ──────────────────────────────────────────────
    // Read
    // ──────────────────────────────────────────────

    public SosAlertResponse getActiveSos() {
        Long userId = securityUtils.getCurrentUserId();
        SosAlert alert = sosAlertRepository
                .findByUserIdAndStatus(userId, SosStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active SOS alert found"));
        return toResponse(alert);
    }

    public List<SosAlertResponse> getSosHistory() {
        Long userId = securityUtils.getCurrentUserId();
        return sosAlertRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SosAlertResponse getSosById(Long alertId) {
        Long userId = securityUtils.getCurrentUserId();
        SosAlert alert = findOwned(alertId, userId);
        return toResponse(alert);
    }

    // ──────────────────────────────────────────────
    // Update Location (during active SOS)
    // ──────────────────────────────────────────────

    @Transactional
    public SosAlertResponse updateLocation(Long alertId, LocationUpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        SosAlert alert = findOwned(alertId, userId);

        if (alert.getStatus() != SosStatus.ACTIVE) {
            throw new IllegalStateException("Cannot update location on a " + alert.getStatus() + " SOS alert");
        }

        alert.setLatitude(request.getLatitude());
        alert.setLongitude(request.getLongitude());
        if (request.getAddress() != null) {
            alert.setAddress(request.getAddress());
        }

        sosAlertRepository.save(alert);
        log.info("📍 Location updated for SOS {} — [{}, {}]",
                alertId, request.getLatitude(), request.getLongitude());
        return toResponse(alert);
    }

    // ──────────────────────────────────────────────
    // Resolve (user is safe)
    // ──────────────────────────────────────────────

    @Transactional
    public SosAlertResponse resolveSos(Long alertId) {
        Long userId = securityUtils.getCurrentUserId();
        SosAlert alert = findOwned(alertId, userId);

        if (alert.getStatus() != SosStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE SOS alerts can be resolved");
        }

        alert.setStatus(SosStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        sosAlertRepository.save(alert);

        notificationService.notifySosResolved(alert.getUser(), alert);
        log.info("✅ SOS {} resolved by user {}", alertId, userId);
        return toResponse(alert);
    }

    // ──────────────────────────────────────────────
    // Cancel (false alarm)
    // ──────────────────────────────────────────────

    @Transactional
    public SosAlertResponse cancelSos(Long alertId) {
        Long userId = securityUtils.getCurrentUserId();
        SosAlert alert = findOwned(alertId, userId);

        if (alert.getStatus() != SosStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE SOS alerts can be cancelled");
        }

        alert.setStatus(SosStatus.CANCELLED);
        alert.setResolvedAt(LocalDateTime.now());
        sosAlertRepository.save(alert);

        notificationService.notifySosCancelled(alert.getUser(), alert);
        log.info("❌ SOS {} cancelled (false alarm) by user {}", alertId, userId);
        return toResponse(alert);
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private SosAlert findOwned(Long alertId, Long userId) {
        return sosAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("SOS alert not found with id: " + alertId));
    }

    private SosAlertResponse toResponse(SosAlert alert) {
        return SosAlertResponse.builder()
                .id(alert.getId())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .address(alert.getAddress())
                .message(alert.getMessage())
                .status(alert.getStatus())
                .contactsNotified(alert.getContactsNotified())
                .googleMapsLink(notificationService.buildMapsLink(alert.getLatitude(), alert.getLongitude()))
                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .build();
    }
}

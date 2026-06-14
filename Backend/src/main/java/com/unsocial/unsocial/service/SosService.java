package com.unsocial.unsocial.service;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.entity.*;
import com.unsocial.unsocial.exception.ResourceNotFoundException;
import com.unsocial.unsocial.repository.EmergencyContactRepository;
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

    private final SosAlertRepository         sosAlertRepository;
    private final EmergencyContactRepository  contactRepository;
    private final NotificationService         notificationService;
    private final SecurityUtils               securityUtils;

    // ──────────────────────────────────────────────
    // Trigger SOS
    // ──────────────────────────────────────────────

    @Transactional
    public SosAlertResponse triggerSos(SosAlertRequest request) {
        User user = securityUtils.getCurrentUser();

        // Only one active SOS at a time
        sosAlertRepository.findByUserIdAndStatus(user.getId(), SosStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "You already have an active SOS alert (ID: " + existing.getId() +
                                    "). Please resolve it before triggering a new one.");
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

        // ── Fetch real emergency contacts and notify ──
        List<NotificationService.ContactInfo> contacts = getContactInfoList(user.getId());
        int notified = notificationService.notifySosTriggered(user, alert, contacts);

        alert.setContactsNotified(notified);
        sosAlertRepository.save(alert);

        log.warn("🚨 SOS triggered by {} — {} contact(s) notified", user.getEmail(), notified);
        return toResponse(alert);
    }

    // ──────────────────────────────────────────────
    // Read
    // ──────────────────────────────────────────────

    public SosAlertResponse getActiveSos() {
        Long userId = securityUtils.getCurrentUserId();
        return toResponse(
                sosAlertRepository.findByUserIdAndStatus(userId, SosStatus.ACTIVE)
                        .orElseThrow(() -> new ResourceNotFoundException("No active SOS alert found"))
        );
    }

    public List<SosAlertResponse> getSosHistory() {
        Long userId = securityUtils.getCurrentUserId();
        return sosAlertRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SosAlertResponse getSosById(Long alertId) {
        Long userId = securityUtils.getCurrentUserId();
        return toResponse(findOwned(alertId, userId));
    }

    // ──────────────────────────────────────────────
    // Update Location (user is moving)
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
        if (request.getAddress() != null) alert.setAddress(request.getAddress());

        sosAlertRepository.save(alert);
        log.info("📍 Location updated for SOS {} → [{}, {}]", alertId, request.getLatitude(), request.getLongitude());
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

        // Notify contacts that user is safe
        List<NotificationService.ContactInfo> contacts = getContactInfoList(userId);
        notificationService.notifySosResolved(alert.getUser(), alert, contacts);

        log.info("✅ SOS {} resolved — contacts notified", alertId);
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

        log.info("❌ SOS {} cancelled (false alarm)", alertId);
        return toResponse(alert);
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private SosAlert findOwned(Long id, Long userId) {
        return sosAlertRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("SOS alert not found with id: " + id));
    }

    private List<NotificationService.ContactInfo> getContactInfoList(Long userId) {
        return contactRepository.findByUserId(userId)
                .stream()
                .map(c -> new NotificationService.ContactInfo(c.getName(), c.getPhone(), c.getEmail()))
                .collect(Collectors.toList());
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

package com.unsocial.unsocial.service;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.entity.*;
import com.unsocial.unsocial.exception.ResourceNotFoundException;
import com.unsocial.unsocial.repository.TrackingSessionRepository;
import com.unsocial.unsocial.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final TrackingSessionRepository trackingSessionRepository;
    private final SecurityUtils securityUtils;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ──────────────────────────────────────────────
    // Start Tracking
    // ──────────────────────────────────────────────

    @Transactional
    public TrackingResponse startTracking(TrackingStartRequest request) {
        User user = securityUtils.getCurrentUser();

        // Only one active session allowed at a time
        trackingSessionRepository.findByUserIdAndStatus(user.getId(), TrackingStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new IllegalStateException(
                            "You already have an active tracking session (ID: " + s.getId() +
                                    "). Stop it before starting a new one."
                    );
                });

        TrackingSession session = TrackingSession.builder()
                .user(user)
                .startLatitude(request.getLatitude())
                .startLongitude(request.getLongitude())
                .currentLatitude(request.getLatitude())
                .currentLongitude(request.getLongitude())
                .currentAddress(request.getAddress())
                .status(TrackingStatus.ACTIVE)
                .build();

        trackingSessionRepository.save(session);
        log.info("Tracking started for user {} — session {}", user.getEmail(), session.getId());
        return toResponse(session);
    }

    // ──────────────────────────────────────────────
    // Update Location
    // ──────────────────────────────────────────────

    @Transactional
    public TrackingResponse updateLocation(Long sessionId, LocationUpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        TrackingSession session = findOwned(sessionId, userId);

        if (session.getStatus() != TrackingStatus.ACTIVE) {
            throw new IllegalStateException("Cannot update location on a STOPPED tracking session");
        }

        session.setCurrentLatitude(request.getLatitude());
        session.setCurrentLongitude(request.getLongitude());
        if (request.getAddress() != null) session.setCurrentAddress(request.getAddress());

        trackingSessionRepository.save(session);
        return toResponse(session);
    }

    // ──────────────────────────────────────────────
    // Stop Tracking
    // ──────────────────────────────────────────────

    @Transactional
    public TrackingResponse stopTracking(Long sessionId) {
        Long userId = securityUtils.getCurrentUserId();
        TrackingSession session = findOwned(sessionId, userId);

        if (session.getStatus() != TrackingStatus.ACTIVE) {
            throw new IllegalStateException("Session is already stopped");
        }

        session.setStatus(TrackingStatus.STOPPED);
        session.setStoppedAt(LocalDateTime.now());
        trackingSessionRepository.save(session);

        log.info("Tracking stopped for session {} by user {}", sessionId, userId);
        return toResponse(session);
    }

    // ──────────────────────────────────────────────
    // Read
    // ──────────────────────────────────────────────

    public TrackingResponse getActiveSession() {
        Long userId = securityUtils.getCurrentUserId();
        TrackingSession session = trackingSessionRepository
                .findByUserIdAndStatus(userId, TrackingStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active tracking session found"));
        return toResponse(session);
    }

    public List<TrackingResponse> getHistory() {
        Long userId = securityUtils.getCurrentUserId();
        return trackingSessionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    // Public share-token lookup (no auth required)
    // ──────────────────────────────────────────────

    public TrackingResponse getByShareToken(String token) {
        TrackingSession session = trackingSessionRepository.findByShareToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking session not found or link has expired"));
        return toResponse(session);
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private TrackingSession findOwned(Long id, Long userId) {
        return trackingSessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking session not found with id: " + id));
    }

    private TrackingResponse toResponse(TrackingSession s) {
        String mapsLink = "https://maps.google.com?q=" + s.getCurrentLatitude() + "," + s.getCurrentLongitude();
        String shareLink = baseUrl + "/api/tracking/share/" + s.getShareToken();

        return TrackingResponse.builder()
                .id(s.getId())
                .startLatitude(s.getStartLatitude())
                .startLongitude(s.getStartLongitude())
                .currentLatitude(s.getCurrentLatitude())
                .currentLongitude(s.getCurrentLongitude())
                .currentAddress(s.getCurrentAddress())
                .shareToken(s.getShareToken())
                .shareLink(shareLink)
                .googleMapsLink(mapsLink)
                .status(s.getStatus())
                .startedAt(s.getCreatedAt())
                .stoppedAt(s.getStoppedAt())
                .build();
    }
}

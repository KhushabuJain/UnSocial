package com.unsocial.unsocial.controller;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.service.SosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sos")
@RequiredArgsConstructor
public class SosController {

    private final SosService sosService;

    /**
     * POST /api/sos/trigger
     * Fire an SOS alert with current GPS coordinates.
     * Notifies all emergency contacts and logs location.
     */
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<SosAlertResponse>> triggerSos(
            @Valid @RequestBody SosAlertRequest request
    ) {
        SosAlertResponse response = sosService.triggerSos(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("🚨 SOS alert triggered. Your contacts have been notified.", response));
    }

    /**
     * GET /api/sos/active
     * Get the currently active SOS alert for the logged-in user.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<SosAlertResponse>> getActiveSos() {
        SosAlertResponse response = sosService.getActiveSos();
        return ResponseEntity.ok(ApiResponse.success("Active SOS alert retrieved", response));
    }

    /**
     * GET /api/sos/history
     * Get full SOS alert history (all statuses, newest first).
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<SosAlertResponse>>> getSosHistory() {
        List<SosAlertResponse> history = sosService.getSosHistory();
        return ResponseEntity.ok(ApiResponse.success("SOS history retrieved", history));
    }

    /**
     * GET /api/sos/{id}
     * Get a specific SOS alert by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SosAlertResponse>> getSosById(
            @PathVariable Long id
    ) {
        SosAlertResponse response = sosService.getSosById(id);
        return ResponseEntity.ok(ApiResponse.success("SOS alert retrieved", response));
    }

    /**
     * PUT /api/sos/{id}/location
     * Update GPS coordinates during an active SOS (user is moving).
     */
    @PutMapping("/{id}/location")
    public ResponseEntity<ApiResponse<SosAlertResponse>> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationUpdateRequest request
    ) {
        SosAlertResponse response = sosService.updateLocation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Location updated", response));
    }

    /**
     * PATCH /api/sos/{id}/resolve
     * Mark SOS as resolved — user is safe.
     * Notifies contacts that the emergency is over.
     */
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<SosAlertResponse>> resolveSos(
            @PathVariable Long id
    ) {
        SosAlertResponse response = sosService.resolveSos(id);
        return ResponseEntity.ok(ApiResponse.success("✅ SOS resolved. Contacts notified that you are safe.", response));
    }

    /**
     * PATCH /api/sos/{id}/cancel
     * Cancel SOS — false alarm.
     * Notifies contacts to disregard the alert.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SosAlertResponse>> cancelSos(
            @PathVariable Long id
    ) {
        SosAlertResponse response = sosService.cancelSos(id);
        return ResponseEntity.ok(ApiResponse.success("SOS cancelled (false alarm). Contacts notified.", response));
    }
}

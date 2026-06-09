package com.unsocial.unsocial.controller;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    /** POST /api/tracking/start — Begin a live tracking session */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<TrackingResponse>> startTracking(
            @Valid @RequestBody TrackingStartRequest request
    ) {
        TrackingResponse response = trackingService.startTracking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Live tracking started. Share the shareLink with your contacts.", response));
    }

    /** PUT /api/tracking/{id}/location — Push a location update */
    @PutMapping("/{id}/location")
    public ResponseEntity<ApiResponse<TrackingResponse>> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Location updated", trackingService.updateLocation(id, request)));
    }

    /** PATCH /api/tracking/{id}/stop — End the session */
    @PatchMapping("/{id}/stop")
    public ResponseEntity<ApiResponse<TrackingResponse>> stopTracking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Tracking stopped", trackingService.stopTracking(id)));
    }

    /** GET /api/tracking/active — Get current active session */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<TrackingResponse>> getActiveSession() {
        return ResponseEntity.ok(ApiResponse.success("Active session retrieved", trackingService.getActiveSession()));
    }

    /** GET /api/tracking/history — Get all past sessions */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TrackingResponse>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.success("History retrieved", trackingService.getHistory()));
    }

    /**
     * GET /api/tracking/share/{token} — PUBLIC endpoint (no JWT needed)
     * Emergency contacts open this link to see live location.
     */
    @GetMapping("/share/{token}")
    public ResponseEntity<ApiResponse<TrackingResponse>> getByShareToken(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.success("Location retrieved", trackingService.getByShareToken(token)));
    }
}

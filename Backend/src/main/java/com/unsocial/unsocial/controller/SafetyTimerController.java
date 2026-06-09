package com.unsocial.unsocial.controller;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.service.SafetyTimerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timer")
@RequiredArgsConstructor
public class SafetyTimerController {

    private final SafetyTimerService safetyTimerService;

    /**
     * POST /api/timer/start
     * Start a countdown timer. If user doesn't check in before expiry,
     * emergency contacts are notified automatically.
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<SafetyTimerResponse>> startTimer(
            @Valid @RequestBody SafetyTimerRequest request
    ) {
        SafetyTimerResponse response = safetyTimerService.startTimer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Safety timer started for " + request.getDurationMinutes() + " minutes. " +
                                "Check in before expiry or your contacts will be alerted.",
                        response
                ));
    }

    /**
     * PATCH /api/timer/{id}/checkin
     * User checks in to confirm they are safe — stops the timer.
     */
    @PatchMapping("/{id}/checkin")
    public ResponseEntity<ApiResponse<SafetyTimerResponse>> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("✅ Checked in! You are marked safe.", safetyTimerService.checkIn(id))
        );
    }

    /**
     * PATCH /api/timer/{id}/cancel
     * Manually cancel an active timer.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SafetyTimerResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Timer cancelled.", safetyTimerService.cancelTimer(id))
        );
    }

    /** GET /api/timer/active — Get the currently running timer */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<SafetyTimerResponse>> getActiveTimer() {
        return ResponseEntity.ok(
                ApiResponse.success("Active timer retrieved", safetyTimerService.getActiveTimer())
        );
    }

    /** GET /api/timer/history — Full timer history */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<SafetyTimerResponse>>> getHistory() {
        return ResponseEntity.ok(
                ApiResponse.success("Timer history retrieved", safetyTimerService.getTimerHistory())
        );
    }
}

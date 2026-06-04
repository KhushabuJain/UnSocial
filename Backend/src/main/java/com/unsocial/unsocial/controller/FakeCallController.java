package com.unsocial.unsocial.controller;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.service.FakeCallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fake-calls")
@RequiredArgsConstructor
public class FakeCallController {

    private final FakeCallService fakeCallService;

    // ──────────────────────────────────────────────
    // Template CRUD
    // ──────────────────────────────────────────────

    /**
     * POST /api/fake-calls/templates
     * Create a new fake call template.
     */
    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<FakeCallResponse>> createTemplate(
            @Valid @RequestBody FakeCallRequest request
    ) {
        FakeCallResponse response = fakeCallService.createTemplate(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fake call template created", response));
    }

    /**
     * GET /api/fake-calls/templates
     * Get all templates for the current user.
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<FakeCallResponse>>> getAllTemplates() {
        List<FakeCallResponse> templates = fakeCallService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success("Templates retrieved", templates));
    }

    /**
     * GET /api/fake-calls/templates/{id}
     * Get a specific template by ID.
     */
    @GetMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<FakeCallResponse>> getTemplateById(
            @PathVariable Long id
    ) {
        FakeCallResponse response = fakeCallService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success("Template retrieved", response));
    }

    /**
     * PUT /api/fake-calls/templates/{id}
     * Update an existing template.
     */
    @PutMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<FakeCallResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody FakeCallRequest request
    ) {
        FakeCallResponse response = fakeCallService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Template updated", response));
    }

    /**
     * DELETE /api/fake-calls/templates/{id}
     * Delete a template.
     */
    @DeleteMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @PathVariable Long id
    ) {
        fakeCallService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted", null));
    }

    /**
     * PATCH /api/fake-calls/templates/{id}/default
     * Mark a template as the default.
     */
    @PatchMapping("/templates/{id}/default")
    public ResponseEntity<ApiResponse<FakeCallResponse>> setDefault(
            @PathVariable Long id
    ) {
        FakeCallResponse response = fakeCallService.setDefault(id);
        return ResponseEntity.ok(ApiResponse.success("Default template updated", response));
    }

    // ──────────────────────────────────────────────
    // Trigger Call
    // ──────────────────────────────────────────────

    /**
     * POST /api/fake-calls/trigger
     * Trigger a fake incoming call.
     * The frontend uses the returned delaySeconds to show a countdown, then rings.
     *
     * Body: { templateId: 1, overrideDelaySeconds: 5 }
     * If templateId is omitted, the user's default template is used.
     */
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<TriggerCallResponse>> triggerCall(
            @RequestBody(required = false) TriggerCallRequest request
    ) {
        if (request == null) request = new TriggerCallRequest();
        TriggerCallResponse response = fakeCallService.triggerCall(request);
        return ResponseEntity.ok(ApiResponse.success("Fake call triggered", response));
    }
}

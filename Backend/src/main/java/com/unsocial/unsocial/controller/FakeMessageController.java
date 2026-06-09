package com.unsocial.unsocial.controller;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.service.FakeMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fake-messages")
@RequiredArgsConstructor
public class FakeMessageController {

    private final FakeMessageService fakeMessageService;

    /** POST /api/fake-messages/templates — Create template */
    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<FakeMessageResponse>> create(
            @Valid @RequestBody FakeMessageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created", fakeMessageService.createTemplate(request)));
    }

    /** GET /api/fake-messages/templates — List all templates */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<FakeMessageResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Templates retrieved", fakeMessageService.getAllTemplates()));
    }

    /** GET /api/fake-messages/templates/{id} — Get one */
    @GetMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<FakeMessageResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Template retrieved", fakeMessageService.getTemplateById(id)));
    }

    /** PUT /api/fake-messages/templates/{id} — Update */
    @PutMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<FakeMessageResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody FakeMessageRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Template updated", fakeMessageService.updateTemplate(id, request)));
    }

    /** DELETE /api/fake-messages/templates/{id} — Delete */
    @DeleteMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        fakeMessageService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted", null));
    }

    /** PATCH /api/fake-messages/templates/{id}/default — Set as default */
    @PatchMapping("/templates/{id}/default")
    public ResponseEntity<ApiResponse<FakeMessageResponse>> setDefault(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Default updated", fakeMessageService.setDefault(id)));
    }

    /**
     * POST /api/fake-messages/trigger
     * Display a fake incoming message on the frontend.
     * Body: { templateId: 1, overrideContent: "optional override" }
     */
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<TriggerMessageResponse>> trigger(
            @RequestBody(required = false) TriggerMessageRequest request
    ) {
        if (request == null) request = new TriggerMessageRequest();
        return ResponseEntity.ok(ApiResponse.success("Message triggered", fakeMessageService.triggerMessage(request)));
    }
}

package com.unsocial.unsocial.service;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.entity.*;
import com.unsocial.unsocial.exception.ResourceNotFoundException;
import com.unsocial.unsocial.repository.FakeCallRepository;
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
public class FakeCallService {

    private final FakeCallRepository fakeCallRepository;
    private final SecurityUtils securityUtils;

    private static final int MAX_TEMPLATES_PER_USER = 10;

    // ──────────────────────────────────────────────
    // Create Template
    // ──────────────────────────────────────────────

    @Transactional
    public FakeCallResponse createTemplate(FakeCallRequest request) {
        User user = securityUtils.getCurrentUser();

        long templateCount = fakeCallRepository.countByUserId(user.getId());
        if (templateCount >= MAX_TEMPLATES_PER_USER) {
            throw new IllegalArgumentException(
                    "Maximum of " + MAX_TEMPLATES_PER_USER + " fake call templates allowed per user"
            );
        }

        // If making this the default, clear existing default first
        if (request.isMakeDefault()) {
            fakeCallRepository.clearDefaultForUser(user.getId());
        }

        FakeCallTemplate template = FakeCallTemplate.builder()
                .user(user)
                .callerName(request.getCallerName().trim())
                .callerPhone(request.getCallerPhone())
                .delaySeconds(request.getDelaySeconds() != null ? request.getDelaySeconds() : 0)
                .ringtone(request.getRingtone() != null ? request.getRingtone() : "classic")
                .isDefault(request.isMakeDefault())
                .build();

        fakeCallRepository.save(template);
        log.info("Created fake call template '{}' for user {}", template.getCallerName(), user.getEmail());
        return toResponse(template);
    }

    // ──────────────────────────────────────────────
    // Read Templates
    // ──────────────────────────────────────────────

    public List<FakeCallResponse> getAllTemplates() {
        Long userId = securityUtils.getCurrentUserId();
        return fakeCallRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public FakeCallResponse getTemplateById(Long templateId) {
        Long userId = securityUtils.getCurrentUserId();
        FakeCallTemplate template = findOwnedTemplate(templateId, userId);
        return toResponse(template);
    }

    // ──────────────────────────────────────────────
    // Update Template
    // ──────────────────────────────────────────────

    @Transactional
    public FakeCallResponse updateTemplate(Long templateId, FakeCallRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        FakeCallTemplate template = findOwnedTemplate(templateId, userId);

        if (request.isMakeDefault() && !template.isDefault()) {
            fakeCallRepository.clearDefaultForUser(userId);
        }

        template.setCallerName(request.getCallerName().trim());
        template.setCallerPhone(request.getCallerPhone());
        template.setDelaySeconds(request.getDelaySeconds() != null ? request.getDelaySeconds() : 0);
        template.setRingtone(request.getRingtone() != null ? request.getRingtone() : "classic");
        template.setDefault(request.isMakeDefault());

        fakeCallRepository.save(template);
        log.info("Updated fake call template {} for user {}", templateId, userId);
        return toResponse(template);
    }

    // ──────────────────────────────────────────────
    // Delete Template
    // ──────────────────────────────────────────────

    @Transactional
    public void deleteTemplate(Long templateId) {
        Long userId = securityUtils.getCurrentUserId();
        FakeCallTemplate template = findOwnedTemplate(templateId, userId);
        fakeCallRepository.delete(template);
        log.info("Deleted fake call template {} for user {}", templateId, userId);
    }

    // ──────────────────────────────────────────────
    // Set Default
    // ──────────────────────────────────────────────

    @Transactional
    public FakeCallResponse setDefault(Long templateId) {
        Long userId = securityUtils.getCurrentUserId();
        fakeCallRepository.clearDefaultForUser(userId);

        FakeCallTemplate template = findOwnedTemplate(templateId, userId);
        template.setDefault(true);
        fakeCallRepository.save(template);

        log.info("Set template {} as default for user {}", templateId, userId);
        return toResponse(template);
    }

    // ──────────────────────────────────────────────
    // Trigger Fake Call
    // ──────────────────────────────────────────────

    /**
     * Returns call details to the frontend.
     * Frontend uses delaySeconds to countdown, then displays the fake incoming call UI.
     */
    public TriggerCallResponse triggerCall(TriggerCallRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        FakeCallTemplate template;

        if (request.getTemplateId() != null) {
            template = findOwnedTemplate(request.getTemplateId(), userId);
        } else {
            // Fall back to user's default template
            template = fakeCallRepository.findByUserIdAndIsDefaultTrue(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No default fake call template found. Please create a template or pass a templateId."
                    ));
        }

        // Override delay if provided in request
        int delay = (request.getOverrideDelaySeconds() != null)
                ? request.getOverrideDelaySeconds()
                : template.getDelaySeconds();

        log.info("Fake call triggered by user {} — caller: '{}', delay: {}s",
                userId, template.getCallerName(), delay);

        return TriggerCallResponse.builder()
                .callerName(template.getCallerName())
                .callerPhone(template.getCallerPhone())
                .ringtone(template.getRingtone())
                .delaySeconds(delay)
                .triggeredAt(LocalDateTime.now())
                .status(delay == 0 ? "IMMEDIATE" : "SCHEDULED")
                .build();
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private FakeCallTemplate findOwnedTemplate(Long templateId, Long userId) {
        return fakeCallRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fake call template not found with id: " + templateId
                ));
    }

    private FakeCallResponse toResponse(FakeCallTemplate template) {
        return FakeCallResponse.builder()
                .id(template.getId())
                .callerName(template.getCallerName())
                .callerPhone(template.getCallerPhone())
                .delaySeconds(template.getDelaySeconds())
                .ringtone(template.getRingtone())
                .isDefault(template.isDefault())
                .createdAt(template.getCreatedAt())
                .build();
    }
}

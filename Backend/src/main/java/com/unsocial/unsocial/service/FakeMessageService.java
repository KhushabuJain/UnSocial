package com.unsocial.unsocial.service;

import com.unsocial.unsocial.dto.*;
import com.unsocial.unsocial.entity.*;
import com.unsocial.unsocial.exception.ResourceNotFoundException;
import com.unsocial.unsocial.repository.FakeMessageRepository;
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
public class FakeMessageService {

    private final FakeMessageRepository fakeMessageRepository;
    private final SecurityUtils securityUtils;

    private static final int MAX_TEMPLATES = 10;

    // ──────────────────────────────────────────────
    // Create
    // ──────────────────────────────────────────────

    @Transactional
    public FakeMessageResponse createTemplate(FakeMessageRequest request) {
        User user = securityUtils.getCurrentUser();

        if (fakeMessageRepository.countByUserId(user.getId()) >= MAX_TEMPLATES) {
            throw new IllegalArgumentException(
                    "Maximum of " + MAX_TEMPLATES + " fake message templates allowed per user"
            );
        }

        if (request.isMakeDefault()) {
            fakeMessageRepository.clearDefaultForUser(user.getId());
        }

        FakeMessage message = FakeMessage.builder()
                .user(user)
                .senderName(request.getSenderName().trim())
                .senderPhone(request.getSenderPhone())
                .messageContent(request.getMessageContent().trim())
                .messageType(request.getMessageType() != null ? request.getMessageType() : MessageType.CUSTOM)
                .isDefault(request.isMakeDefault())
                .build();

        fakeMessageRepository.save(message);
        log.info("Created fake message template for user {}", user.getEmail());
        return toResponse(message);
    }

    // ──────────────────────────────────────────────
    // Read
    // ──────────────────────────────────────────────

    public List<FakeMessageResponse> getAllTemplates() {
        Long userId = securityUtils.getCurrentUserId();
        return fakeMessageRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public FakeMessageResponse getTemplateById(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        return toResponse(findOwned(id, userId));
    }

    // ──────────────────────────────────────────────
    // Update
    // ──────────────────────────────────────────────

    @Transactional
    public FakeMessageResponse updateTemplate(Long id, FakeMessageRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        FakeMessage message = findOwned(id, userId);

        if (request.isMakeDefault() && !message.isDefault()) {
            fakeMessageRepository.clearDefaultForUser(userId);
        }

        message.setSenderName(request.getSenderName().trim());
        message.setSenderPhone(request.getSenderPhone());
        message.setMessageContent(request.getMessageContent().trim());
        message.setMessageType(request.getMessageType() != null ? request.getMessageType() : MessageType.CUSTOM);
        message.setDefault(request.isMakeDefault());

        fakeMessageRepository.save(message);
        return toResponse(message);
    }

    // ──────────────────────────────────────────────
    // Delete
    // ──────────────────────────────────────────────

    @Transactional
    public void deleteTemplate(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        fakeMessageRepository.delete(findOwned(id, userId));
        log.info("Deleted fake message template {} for user {}", id, userId);
    }

    // ──────────────────────────────────────────────
    // Set Default
    // ──────────────────────────────────────────────

    @Transactional
    public FakeMessageResponse setDefault(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        fakeMessageRepository.clearDefaultForUser(userId);
        FakeMessage message = findOwned(id, userId);
        message.setDefault(true);
        fakeMessageRepository.save(message);
        return toResponse(message);
    }

    // ──────────────────────────────────────────────
    // Trigger
    // ──────────────────────────────────────────────

    public TriggerMessageResponse triggerMessage(TriggerMessageRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        FakeMessage template;
        if (request.getTemplateId() != null) {
            template = findOwned(request.getTemplateId(), userId);
        } else {
            template = fakeMessageRepository.findByUserIdAndIsDefaultTrue(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No default fake message template set. Please create one or pass a templateId."
                    ));
        }

        String content = (request.getOverrideContent() != null && !request.getOverrideContent().isBlank())
                ? request.getOverrideContent()
                : template.getMessageContent();

        log.info("Fake message triggered by user {} — sender: '{}'", userId, template.getSenderName());

        return TriggerMessageResponse.builder()
                .senderName(template.getSenderName())
                .senderPhone(template.getSenderPhone())
                .messageContent(content)
                .messageType(template.getMessageType())
                .triggeredAt(LocalDateTime.now())
                .build();
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private FakeMessage findOwned(Long id, Long userId) {
        return fakeMessageRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Fake message template not found with id: " + id));
    }

    private FakeMessageResponse toResponse(FakeMessage m) {
        return FakeMessageResponse.builder()
                .id(m.getId())
                .senderName(m.getSenderName())
                .senderPhone(m.getSenderPhone())
                .messageContent(m.getMessageContent())
                .messageType(m.getMessageType())
                .isDefault(m.isDefault())
                .createdAt(m.getCreatedAt())
                .build();
    }
}

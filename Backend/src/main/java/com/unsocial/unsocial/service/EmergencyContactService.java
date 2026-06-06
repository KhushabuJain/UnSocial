package com.unsocial.unsocial.service;

import com.unsocial.unsocial.dto.EmergencyContactRequest;
import com.unsocial.unsocial.dto.EmergencyContactResponse;
import com.unsocial.unsocial.entity.EmergencyContact;
import com.unsocial.unsocial.entity.User;
import com.unsocial.unsocial.exception.ResourceNotFoundException;
import com.unsocial.unsocial.repository.EmergencyContactRepository;
import com.unsocial.unsocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyContactService {

    private static final int MAX_CONTACTS = 5;

    private final EmergencyContactRepository contactRepository;
    private final UserRepository userRepository;

    // ──────────────────────────────────────────────
    // Create
    // ──────────────────────────────────────────────




    @Transactional
    public EmergencyContactResponse addContact(String userEmail, EmergencyContactRequest request) {
        User user = getUser(userEmail);

        // Enforce max contacts limit
        int count = contactRepository.countByUserId(user.getId());
        if (count >= MAX_CONTACTS) {
            throw new IllegalArgumentException(
                    "Maximum " + MAX_CONTACTS + " emergency contacts allowed"
            );
        }

        // If this contact is being set as primary, remove primary from existing ones
        if (request.isPrimary()) {
            clearExistingPrimary(user.getId());
        }

        // If it's the first contact, auto-set as primary
        boolean autoPrimary = (count == 0) || request.isPrimary();

        EmergencyContact contact = EmergencyContact.builder()
                .user(user)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .relationship(request.getRelationship())
                .isPrimary(autoPrimary)
                .notifyOnSos(request.isNotifyOnSos())
                .build();

        EmergencyContact saved = contactRepository.save(contact);
        log.info("Added emergency contact [{}] for user [{}]", saved.getId(), userEmail);

        return EmergencyContactResponse.from(saved);
    }

    // ──────────────────────────────────────────────
    // Read
    // ──────────────────────────────────────────────

    public List<EmergencyContactResponse> getAllContacts(String userEmail) {
        User user = getUser(userEmail);
        return contactRepository
                .findByUserIdOrderByIsPrimaryDescCreatedAtAsc(user.getId())
                .stream()
                .map(EmergencyContactResponse::from)
                .toList();
    }

    public EmergencyContactResponse getContact(String userEmail, Long contactId) {
        User user = getUser(userEmail);
        EmergencyContact contact = getContactOwnedByUser(contactId, user.getId());
        return EmergencyContactResponse.from(contact);
    }

    // ──────────────────────────────────────────────
    // Update
    // ──────────────────────────────────────────────

    @Transactional
    public EmergencyContactResponse updateContact(
            String userEmail, Long contactId, EmergencyContactRequest request
    ) {
        User user = getUser(userEmail);
        EmergencyContact contact = getContactOwnedByUser(contactId, user.getId());

        // If promoting this contact to primary, demote existing primary
        if (request.isPrimary() && !contact.isPrimary()) {
            clearExistingPrimary(user.getId());
        }

        contact.setName(request.getName());
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setRelationship(request.getRelationship());
        contact.setPrimary(request.isPrimary());
        contact.setNotifyOnSos(request.isNotifyOnSos());

        EmergencyContact updated = contactRepository.save(contact);
        log.info("Updated emergency contact [{}] for user [{}]", contactId, userEmail);

        return EmergencyContactResponse.from(updated);
    }

    // ──────────────────────────────────────────────
    // Delete
    // ──────────────────────────────────────────────

    @Transactional
    public void deleteContact(String userEmail, Long contactId) {
        User user = getUser(userEmail);
        EmergencyContact contact = getContactOwnedByUser(contactId, user.getId());

        contactRepository.delete(contact);
        log.info("Deleted emergency contact [{}] for user [{}]", contactId, userEmail);

        // If deleted contact was the primary, auto-promote the oldest remaining contact
        if (contact.isPrimary()) {
            contactRepository
                    .findByUserIdOrderByIsPrimaryDescCreatedAtAsc(user.getId())
                    .stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setPrimary(true);
                        contactRepository.save(next);
                        log.info("Auto-promoted contact [{}] to primary", next.getId());
                    });
        }
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private EmergencyContact getContactOwnedByUser(Long contactId, Long userId) {
        return contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency contact", contactId));
    }

    private void clearExistingPrimary(Long userId) {
        contactRepository.findByUserIdAndIsPrimaryTrue(userId)
                .ifPresent(existing -> {
                    existing.setPrimary(false);
                    contactRepository.save(existing);
                });
    }
}

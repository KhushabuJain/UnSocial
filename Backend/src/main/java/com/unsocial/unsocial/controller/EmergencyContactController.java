package com.unsocial.unsocial.controller;

import com.unsocial.unsocial.dto.ApiResponse;
import com.unsocial.unsocial.dto.EmergencyContactRequest;
import com.unsocial.unsocial.dto.EmergencyContactResponse;
import com.unsocial.unsocial.service.EmergencyContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class EmergencyContactController {

    private final EmergencyContactService contactService;

    /**
     * POST /api/contacts
     * Add a new emergency contact (max 5 per user).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> addContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EmergencyContactRequest request
    ) {
        EmergencyContactResponse response = contactService.addContact(
                userDetails.getUsername(), request
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Emergency contact added", response));
    }

    /**
     * GET /api/contacts
     * Get all emergency contacts for the logged-in user.
     * Primary contact appears first.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmergencyContactResponse>>> getAllContacts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<EmergencyContactResponse> contacts = contactService.getAllContacts(
                userDetails.getUsername()
        );
        return ResponseEntity.ok(
                ApiResponse.success("Contacts retrieved", contacts)
        );
    }

    /**
     * GET /api/contacts/{id}
     * Get a single contact by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> getContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        EmergencyContactResponse response = contactService.getContact(
                userDetails.getUsername(), id
        );
        return ResponseEntity.ok(ApiResponse.success("Contact retrieved", response));
    }

    /**
     * PUT /api/contacts/{id}
     * Update an existing contact.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> updateContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody EmergencyContactRequest request
    ) {
        EmergencyContactResponse response = contactService.updateContact(
                userDetails.getUsername(), id, request
        );
        return ResponseEntity.ok(ApiResponse.success("Contact updated", response));
    }

    /**
     * DELETE /api/contacts/{id}
     * Delete a contact. If deleted contact was primary,
     * the next oldest contact is auto-promoted.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        contactService.deleteContact(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Contact deleted", null));
    }
}

package com.unsocial.unsocial.dto;

import com.unsocial.unsocial.entity.EmergencyContact;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactResponse {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String relationship;
    private boolean isPrimary;
    private boolean notifyOnSos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Convenience mapper — no MapStruct dependency needed
    public static EmergencyContactResponse from(EmergencyContact contact) {
        return EmergencyContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .phone(contact.getPhone())
                .email(contact.getEmail())
                .relationship(contact.getRelationship())
                .isPrimary(contact.isPrimary())
                .notifyOnSos(contact.isNotifyOnSos())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}

package com.unsocial.unsocial.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Stores emergency contacts for a user.
 * These people receive real email + SMS alerts on SOS or timer expiry.
 */
@Entity
@Table(name = "emergency_contacts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    // Email for real alert notifications
    @Column(unique = true)
    private String email;

    @Column
    private String relationship;

    @Column(name = "is_primary")
    @Builder.Default
    private boolean isPrimary = false;

    @Column(name = "notify_on_sos"  , nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    @Builder.Default
    private boolean notifyOnSos = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



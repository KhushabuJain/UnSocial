package com.unsocial.unsocial.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fake_call_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FakeCallTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "caller_name", nullable = false)
    private String callerName;

    @Column(name = "caller_phone",unique = true,nullable = false)
    private String callerPhone;

    // Delay in seconds before the fake call rings (0 = immediate)
    @Column(name = "delay_seconds")
    @Builder.Default
    private int delaySeconds = 0;

    // Which built-in ringtone to play on the frontend when this call rings
    @Column(name = "ringtone")
    @Builder.Default
    private String ringtone = "classic";

    @Column(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;

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

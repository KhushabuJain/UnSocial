package com.unsocial.unsocial.service;

import com.unsocial.unsocial.entity.SosAlert;
import com.unsocial.unsocial.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Central notification orchestrator.
 *
 * IMPORTANT — two different audiences, two different emails:
 *   1. Emergency CONTACTS get the real emergency alert (sendSosAlertToContact)
 *   2. The USER who triggered SOS gets a confirmation it was sent (sendSosConfirmationToUser)
 * These must NEVER be swapped — a contact should never see "Hi Khushabu, your alert was sent"
 * and the user should never receive the raw "emergency, come help" template addressed to them.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;
    private final SmsService   smsService;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    // ──────────────────────────────────────────────
    // SOS Triggered
    // ──────────────────────────────────────────────

    public int notifySosTriggered(User user, SosAlert alert, List<ContactInfo> contacts) {
        String mapsLink = buildMapsLink(alert.getLatitude(), alert.getLongitude());

        if (contacts == null || contacts.isEmpty()) {
            log.warn("⚠️ No emergency contacts found for user {} — nobody will be alerted!", user.getEmail());
        } else {
            for (ContactInfo contact : contacts) {
                log.warn("🚨 Notifying {} ({}) about SOS from {}", contact.name(), contact.phone(), user.getFullName());

                // → Real alert email goes to the CONTACT
                if (emailEnabled && contact.email() != null && !contact.email().isBlank()) {
                    emailService.sendSosAlertToContact(
                            contact.email(),
                            contact.name(),
                            user.getFullName(),
                            mapsLink,
                            alert.getAddress(),
                            alert.getMessage(),
                            alert.getCreatedAt()
                    );
                } else {
                    log.warn("⚠️ Contact '{}' has no email on file — only SMS will be attempted", contact.name());
                }

                // → Real SMS goes to the CONTACT
                smsService.sendSosAlert(contact.phone(), contact.name(),
                        user.getFullName(), mapsLink, alert.getMessage());
            }
        }

        // → Confirmation email goes to the USER who triggered SOS (never to a contact!)
        if (emailEnabled && user.getEmail() != null) {
            emailService.sendSosConfirmationToUser(
                    user.getEmail(),
                    user.getFullName(),
                    mapsLink,
                    contacts == null ? 0 : contacts.size()
            );
        }

        int count = contacts == null ? 0 : contacts.size();
        log.warn("🚨 SOS dispatched — {} contact(s) alerted, confirmation sent to {}", count, user.getEmail());
        return count;
    }

    // ──────────────────────────────────────────────
    // SOS Resolved — tells CONTACTS the user is safe now
    // ──────────────────────────────────────────────

    public void notifySosResolved(User user, SosAlert alert, List<ContactInfo> contacts) {
        if (contacts == null || contacts.isEmpty()) return;

        for (ContactInfo contact : contacts) {
            if (emailEnabled && contact.email() != null && !contact.email().isBlank()) {
                emailService.sendSosResolvedToContact(contact.email(), contact.name(), user.getFullName());
            }
            smsService.sendResolvedAlert(contact.phone(), contact.name(), user.getFullName());
        }
        log.info("✅ 'All clear' notifications sent to {} contact(s)", contacts.size());
    }

    // ──────────────────────────────────────────────
    // Safety Timer Expired — tells CONTACTS the user didn't check in
    // ──────────────────────────────────────────────

    public void notifyTimerExpired(User user, String note, List<ContactInfo> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            log.warn("⚠️ No emergency contacts for user {} — timer expired with nobody to notify", user.getEmail());
            return;
        }

        for (ContactInfo contact : contacts) {
            if (emailEnabled && contact.email() != null && !contact.email().isBlank()) {
                emailService.sendTimerExpiredAlert(contact.email(), contact.name(), user.getFullName(), note);
            }
            smsService.sendTimerExpiredAlert(contact.phone(), contact.name(), user.getFullName(), note);
        }
        log.warn("⏰ Timer-expired notifications sent to {} contact(s) for user {}", contacts.size(), user.getEmail());
    }

    // ──────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────

    public String buildMapsLink(Double lat, Double lng) {
        if (lat == null || lng == null) return "Location not available";
        return "https://maps.google.com?q=" + lat + "," + lng;
    }

    // ──────────────────────────────────────────────
    // ContactInfo record (passed by SosService / SafetyTimerService)
    // ──────────────────────────────────────────────

    public record ContactInfo(String name, String phone, String email) {}
}

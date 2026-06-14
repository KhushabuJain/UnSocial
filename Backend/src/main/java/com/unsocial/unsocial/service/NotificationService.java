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
 * Sends real email + SMS (optional) to emergency contacts on SOS / timer expiry.
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

    /**
     * Notify all emergency contacts that the user triggered an SOS.
     * contacts = list of [name, phone, email] for each emergency contact.
     */
    public int notifySosTriggered(User user, SosAlert alert, List<ContactInfo> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            log.warn("No emergency contacts found for user {} — nobody to notify!", user.getEmail());
            return 0;
        }

        String mapsLink = buildMapsLink(alert.getLatitude(), alert.getLongitude());

        for (ContactInfo contact : contacts) {
            log.warn("🚨 Notifying {} ({}) about SOS from {}", contact.name(), contact.phone(), user.getFullName());

            // Real email
            if (emailEnabled && contact.email() != null && !contact.email().isBlank()) {
                emailService.sendSosAlert(
                        contact.email(),
                        contact.name(),
                        user.getFullName(),
                        mapsLink,
                        null,
                        alert.getMessage(),
                        alert.getCreatedAt()
                );
            }

            // Real SMS
            smsService.sendSosAlert(contact.phone(), contact.name(),
                    user.getFullName(), mapsLink, alert.getMessage());
        }

        log.warn("🚨 SOS notifications dispatched to {} contact(s) for user {}", contacts.size(), user.getEmail());
        return contacts.size();
    }

    // ──────────────────────────────────────────────
    // SOS Resolved
    // ──────────────────────────────────────────────

    public void notifySosResolved(User user, SosAlert alert, List<ContactInfo> contacts) {
        if (contacts == null || contacts.isEmpty()) return;

        for (ContactInfo contact : contacts) {
            if (emailEnabled && contact.email() != null && !contact.email().isBlank()) {
                emailService.sendSosResolvedEmail(
                        contact.email(),
                        user.getFullName(),
                        buildMapsLink(alert.getLatitude(), alert.getLongitude()),
                        contacts.size()
                );
            }
            smsService.sendResolvedAlert(contact.phone(), contact.name(), user.getFullName());
        }
        log.info("✅ Resolved notifications sent to {} contact(s)", contacts.size());
    }

    // ──────────────────────────────────────────────
    // Safety Timer Expired
    // ──────────────────────────────────────────────

    public void notifyTimerExpired(User user, String note, List<ContactInfo> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            log.warn("No emergency contacts for user {} — timer expired with nobody to notify", user.getEmail());
            return;
        }

        for (ContactInfo contact : contacts) {
            if (emailEnabled && contact.email() != null && !contact.email().isBlank()) {
                emailService.sendTimerExpiredEmail(
                        contact.email(),
                        contact.name(),
                        user.getFullName(),
                        note
                );
            }
            smsService.sendTimerExpiredAlert(contact.phone(), contact.name(), user.getFullName(), note);
        }
        log.warn("⏰ Timer expired notifications sent to {} contact(s) for user {}", contacts.size(), user.getEmail());
    }

    // ──────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────

    public String buildMapsLink(Double lat, Double lng) {
        if (lat == null || lng == null) return "Location not available";
        return "https://maps.google.com?q=" + lat + "," + lng;
    }

    // ──────────────────────────────────────────────
    // ContactInfo record (passed by SosService)
    // ──────────────────────────────────────────────

    public record ContactInfo(String name, String phone, String email) {}
}

package com.unsocial.unsocial.service;

import com.unsocial.unsocial.entity.SosAlert;
import com.unsocial.unsocial.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handles push notifications to emergency contacts.
 *
 * Current implementation: logs to console.
 * TODO: Replace log calls with Firebase Cloud Messaging (FCM) API calls.
 *       Each contact should receive a push notification with the user's
 *       name, location link, and custom message.
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * Notify all emergency contacts of the user about an active SOS.
     *
     * @param user     the user in distress
     * @param alert    the SOS alert (contains location + message)
     * @param contacts number of contacts to notify (from EmergencyContact module)
     */
    public void notifySosTriggered(User user, SosAlert alert, int contacts) {
        String mapsLink = buildMapsLink(alert.getLatitude(), alert.getLongitude());

        log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.warn("🚨 SOS ALERT TRIGGERED");
        log.warn("👤 User     : {} ({})", user.getName(), user.getEmail());
        log.warn("📍 Location : {}", mapsLink);
        if (alert.getAddress() != null) {
            log.warn("🏠 Address  : {}", alert.getAddress());
        }
        if (alert.getMessage() != null) {
            log.warn("💬 Message  : {}", alert.getMessage());
        }
        log.warn("📱 Notifying {} emergency contact(s)", contacts);
        log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // ─── Firebase integration goes here ───────────────────────────
        // for (EmergencyContact contact : contacts) {
        //     fcmService.sendNotification(
        //         contact.getFcmToken(),
        //         "🚨 " + user.getFullName() + " needs help!",
        //         "Tap to see their location",
        //         Map.of("mapsLink", mapsLink, "alertId", alert.getId().toString())
        //     );
        // }
        // ──────────────────────────────────────────────────────────────
    }

    /**
     * Notify contacts that the user is now safe (SOS resolved).
     */
    public void notifySosResolved(User user, SosAlert alert) {
        log.info("✅ SOS RESOLVED — {} is safe. Alert ID: {}", user.getName(), alert.getId());

        // TODO: Send FCM "User is safe" notification to emergency contacts
    }

    /**
     * Notify contacts that the SOS was a false alarm (cancelled).
     */
    public void notifySosCancelled(User user, SosAlert alert) {
        log.info("❌ SOS CANCELLED (false alarm) — User: {}, Alert ID: {}",
                user.getName(), alert.getId());

        // TODO: Send FCM "False alarm" notification to emergency contacts
    }

    // ──────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────

    public String buildMapsLink(Double lat, Double lng) {
        return "https://maps.google.com?q=" + lat + "," + lng;
    }
}

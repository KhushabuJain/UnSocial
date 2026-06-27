package com.unsocial.unsocial.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends emergency alerts over WhatsApp using Twilio's WhatsApp Business API.
 * Reuses the same Twilio account/credentials as SmsService — only the
 * "from"/"to" numbers are prefixed with "whatsapp:" to route over that channel.
 *
 * Setup notes (do this in the Twilio console before enabling):
 *  1. Activate a WhatsApp sender — for development, join the Twilio WhatsApp
 *     Sandbox (Messaging > Try it out > Send a WhatsApp message) and have
 *     each recipient send the given join code to the sandbox number once.
 *  2. For production, apply for a WhatsApp-enabled Twilio number via Meta.
 *  3. Set twilio.whatsapp-number below to that sender (no "whatsapp:" prefix,
 *     it's added automatically) and set notification.whatsapp.enabled=true.
 */
@Slf4j
@Service
public class WhatsAppService {

    @Value("${twilio.whatsapp-number:}")
    private String fromNumber;

    @Value("${notification.whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    @Value("${app.name:UnSocial}")
    private String appName;

    // ──────────────────────────────────────────────
    // SOS WhatsApp message
    // ──────────────────────────────────────────────

    @Async
    public void sendSosAlert(String toPhone, String contactName,
                             String userName, String mapsLink, String customMessage) {
        if (!whatsappEnabled) {
            log.info("WhatsApp disabled — skipping WhatsApp alert to {}", toPhone);
            return;
        }
        try {
            String body = String.format(
                    """
                            🚨 *SOS ALERT from %s!*
                            
                            Hi %s, %s may need immediate help.
                            
                            📍 Location: %s
                            
                            %s\
                            Please call them or contact 112 immediately.
                            
                            — %s Safety""",
                    userName, contactName, userName,
                    mapsLink,
                    customMessage != null && !customMessage.isBlank()
                            ? "Message: \"" + customMessage + "\"\n\n"
                            : "",
                    appName
            );
            send(toPhone, body);
            log.info("✅ SOS WhatsApp message sent to {}", toPhone);
        } catch (Exception e) {
            log.error("❌ Failed to send SOS WhatsApp message to {}: {}", toPhone, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // Resolved WhatsApp message
    // ──────────────────────────────────────────────

    @Async
    public void sendResolvedAlert(String toPhone, String contactName, String userName) {
        if (!whatsappEnabled) return;
        try {
            String body = String.format(
                    """
                            ✅ *ALL CLEAR — %s is safe!*
                            
                            Hi %s, %s has marked themselves as safe. No further action needed.
                            
                            — %s Safety""",
                    userName, contactName, userName, appName
            );
            send(toPhone, body);
            log.info("✅ Resolved WhatsApp message sent to {}", toPhone);
        } catch (Exception e) {
            log.error("❌ Failed to send resolved WhatsApp message to {}: {}", toPhone, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // Timer Expired WhatsApp message
    // ──────────────────────────────────────────────

    @Async
    public void sendTimerExpiredAlert(String toPhone, String contactName,
                                      String userName, String note) {
        if (!whatsappEnabled) return;
        try {
            String body = String.format(
                    """
                            ⏰ *SAFETY TIMER EXPIRED — %s did not check in!*
                            
                            Hi %s, %s's safety timer expired without check-in.%s
                            
                            Please call them or contact 112 immediately.
                            
                            — %s Safety""",
                    userName, contactName, userName,
                    note != null && !note.isBlank() ? "\nNote: \"" + note + "\"" : "",
                    appName
            );
            send(toPhone, body);
            log.info("✅ Timer expired WhatsApp message sent to {}", toPhone);
        } catch (Exception e) {
            log.error("❌ Failed to send timer expired WhatsApp message to {}: {}", toPhone, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private void send(String toPhone, String body) {
        Message.creator(
                new PhoneNumber("whatsapp:" + normalizePhone(toPhone)),
                new PhoneNumber("whatsapp:" + normalizePhone(fromNumber)),
                body
        ).create();
    }

    private String normalizePhone(String phone) {
        if (phone == null) return phone;
        phone = phone.trim().replaceAll("[\\s-()]", "");
        if (!phone.startsWith("+")) {
            phone = "+91" + phone;
        }
        return phone;
    }
}

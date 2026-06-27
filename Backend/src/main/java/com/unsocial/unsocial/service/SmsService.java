package com.unsocial.unsocial.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    @Value("${twilio.phone-number:}")
    private String fromNumber;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.name:UnSocial}")
    private String appName;

    // ──────────────────────────────────────────────
    // SOS SMS
    // ──────────────────────────────────────────────

    @Async
    public void sendSosAlert(String toPhone, String contactName,
                             String userName, String mapsLink, String customMessage) {
        if (!smsEnabled) {
            log.info("SMS disabled — skipping SMS to {}", toPhone);
            return;
        }
        try {
            String body = String.format(
                    """
                            🚨 SOS ALERT from %s!
                            
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

            Message.creator(
                new PhoneNumber(normalizePhone(toPhone)),
                new PhoneNumber(fromNumber),
                body
            ).create();

            log.info("✅ SOS SMS sent to {}", toPhone);

        } catch (Exception e) {
            log.error("❌ Failed to send SOS SMS to {}: {}", toPhone, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // Resolved SMS
    // ──────────────────────────────────────────────

    @Async
    public void sendResolvedAlert(String toPhone, String contactName, String userName) {
        if (!smsEnabled) return;
        try {
            String body = String.format(
                    """
                            ✅ ALL CLEAR — %s is safe!
                            
                            Hi %s, %s has marked themselves as safe. No further action needed.
                            
                            — %s Safety""",
                userName, contactName, userName, appName
            );

            Message.creator(
                new PhoneNumber(normalizePhone(toPhone)),
                new PhoneNumber(fromNumber),
                body
            ).create();

            log.info("✅ Resolved SMS sent to {}", toPhone);

        } catch (Exception e) {
            log.error("❌ Failed to send resolved SMS to {}: {}", toPhone, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // Timer Expired SMS
    // ──────────────────────────────────────────────

    @Async
    public void sendTimerExpiredAlert(String toPhone, String contactName,
                                      String userName, String note) {
        if (!smsEnabled) return;
        try {
            String body = String.format(
                    """
                            ⏰ SAFETY TIMER EXPIRED — %s did not check in!
                            
                            Hi %s, %s's safety timer expired without check-in.%s
                            
                            Please call them or contact 112 immediately.
                            
                            — %s Safety""",
                userName, contactName, userName,
                note != null && !note.isBlank() ? "\nNote: \"" + note + "\"" : "",
                appName
            );

            Message.creator(
                new PhoneNumber(normalizePhone(toPhone)),
                new PhoneNumber(fromNumber),
                body
            ).create();

            log.info("✅ Timer expired SMS sent to {}", toPhone);

        } catch (Exception e) {
            log.error("❌ Failed to send timer expired SMS to {}: {}", toPhone, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // Helper — ensure phone has country code
    // ──────────────────────────────────────────────

    private String normalizePhone(String phone) {
        if (phone == null) return phone;
        phone = phone.trim().replaceAll("[\\s-()]", "");
        // Add India country code (+91) if no country code present
        if (!phone.startsWith("+")) {
            phone = "+91" + phone;
        }
        return phone;
    }
}

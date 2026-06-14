package com.unsocial.unsocial.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:UnSocial}")
    private String appName;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    // ──────────────────────────────────────────────
    // SOS Alert Email to Emergency Contact
    // ──────────────────────────────────────────────

    @Async
    public void sendSosAlert(
            String toEmail,
            String contactName,
            String userName,
            String mapsLink,
            String address,
            String message,
            LocalDateTime triggeredAt
    ) {
        if (!emailEnabled) {
            log.info("[EMAIL DISABLED] Would have sent SOS alert to {}", toEmail);
            return;
        }

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromEmail, appName + " Safety");
            helper.setTo(toEmail);
            helper.setSubject("🚨 EMERGENCY: " + userName + " needs your help NOW!");
            helper.setText(buildSosEmailHtml(contactName, userName, mapsLink, address, message, triggeredAt), true);

            mailSender.send(mime);
            log.info("📧 SOS alert email sent to {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // Confirmation Email to the User (SOS triggered)
    // ──────────────────────────────────────────────

    @Async
    public void sendSosResolvedEmail(
            String toEmail,
            String userName,
            String mapsLink,
            int contactsNotified
    ) {
        if (!emailEnabled) return;

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromEmail, appName + " Safety");
            helper.setTo(toEmail);
            helper.setSubject("✅ Your SOS alert has been sent");
            helper.setText(buildConfirmationEmailHtml(userName, mapsLink, contactsNotified), true);

            mailSender.send(mime);
            log.info("📧 SOS confirmation email sent to user {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send confirmation email: {}", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // Timer Expired Email
    // ──────────────────────────────────────────────

    @Async
    public void sendTimerExpiredEmail(
            String toEmail,
            String contactName,
            String userName,
            String note
    ) {
        if (!emailEnabled) return;

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromEmail, appName + " Safety");
            helper.setTo(toEmail);
            helper.setSubject("⏰ ALERT: " + userName + "'s safety timer expired without check-in");
            helper.setText(buildTimerExpiredHtml(contactName, userName, note), true);

            mailSender.send(mime);
            log.info("📧 Timer expiry alert sent to {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send timer alert: {}", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // HTML Email Templates
    // ──────────────────────────────────────────────

    private String buildSosEmailHtml(String contactName, String userName, String mapsLink,
                                      String address, String message, LocalDateTime time) {
        String timeStr = time != null
                ? time.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                : "Just now";
        String addressRow = address != null
                ? "<tr><td style='padding:6px 0;color:#6b7280;font-size:14px;'>📍 Address</td>"
                + "<td style='padding:6px 0;font-size:14px;font-weight:500;'>" + address + "</td></tr>"
                : "";
        String messageRow = message != null
                ? "<tr><td style='padding:6px 0;color:#6b7280;font-size:14px;'>💬 Message</td>"
                + "<td style='padding:6px 0;font-size:14px;font-style:italic;'>\"" + message + "\"</td></tr>"
                : "";

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"/></head>
            <body style="margin:0;padding:0;background:#f3f4f6;font-family:Arial,sans-serif;">
              <div style="max-width:580px;margin:32px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                
                <!-- Header -->
                <div style="background:#dc2626;padding:32px;text-align:center;">
                  <div style="font-size:48px;margin-bottom:8px;">🚨</div>
                  <h1 style="color:#fff;font-size:24px;margin:0;font-weight:700;">Emergency Alert</h1>
                  <p style="color:#fca5a5;font-size:14px;margin:8px 0 0;">Sent via %s Safety Platform</p>
                </div>
                
                <!-- Body -->
                <div style="padding:32px;">
                  <p style="font-size:16px;color:#111827;margin:0 0 16px;">Hi <strong>%s</strong>,</p>
                  <p style="font-size:15px;color:#374151;line-height:1.6;margin:0 0 24px;">
                    <strong style="color:#dc2626;">%s</strong> has triggered an <strong>emergency SOS alert</strong> 
                    and needs your immediate help. Please check on them right now.
                  </p>
                  
                  <!-- Alert Details -->
                  <div style="background:#fef2f2;border:1.5px solid #fca5a5;border-radius:12px;padding:20px;margin:0 0 24px;">
                    <h3 style="font-size:13px;font-weight:600;color:#991b1b;text-transform:uppercase;letter-spacing:0.05em;margin:0 0 12px;">Alert Details</h3>
                    <table style="width:100%;border-collapse:collapse;">
                      %s
                      <tr><td style='padding:6px 0;color:#6b7280;font-size:14px;'>⏰ Time</td>
                        <td style='padding:6px 0;font-size:14px;font-weight:500;'>%s</td></tr>
                      %s
                    </table>
                  </div>
                  
                  <!-- Map Button -->
                  <a href="%s" style="display:block;background:#7c3aed;color:#fff;text-decoration:none;text-align:center;padding:14px 24px;border-radius:10px;font-weight:700;font-size:15px;margin:0 0 24px;">
                    📍 Open Location in Google Maps
                  </a>
                  
                  <p style="font-size:14px;color:#4b5563;line-height:1.6;margin:0;">
                    Please <strong>call them immediately</strong> or go to their location. 
                    If you cannot reach them, consider contacting emergency services (112 in India).
                  </p>
                </div>
                
                <!-- Footer -->
                <div style="background:#f9fafb;border-top:1px solid #e5e7eb;padding:20px 32px;">
                  <p style="font-size:12px;color:#9ca3af;margin:0;text-align:center;">
                    This alert was automatically sent by the %s app because %s activated their emergency SOS.
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(appName, contactName, userName,
                    addressRow, timeStr, messageRow, mapsLink, appName, userName);
    }

    private String buildConfirmationEmailHtml(String userName, String mapsLink, int contactsNotified) {
        return """
            <!DOCTYPE html><html><head><meta charset="UTF-8"/></head>
            <body style="margin:0;padding:0;background:#f3f4f6;font-family:Arial,sans-serif;">
              <div style="max-width:580px;margin:32px auto;background:#fff;border-radius:16px;overflow:hidden;">
                <div style="background:#7c3aed;padding:28px;text-align:center;">
                  <h1 style="color:#fff;font-size:20px;margin:0;">✅ SOS Alert Sent</h1>
                </div>
                <div style="padding:28px;">
                  <p style="font-size:15px;color:#111827;">Hi <strong>%s</strong>,</p>
                  <p style="font-size:14px;color:#374151;line-height:1.6;">
                    Your SOS alert has been sent to <strong>%d emergency contact(s)</strong>. 
                    They have received your location and will be reaching out to you shortly.
                  </p>
                  <a href="%s" style="display:block;background:#059669;color:#fff;text-decoration:none;text-align:center;padding:12px;border-radius:8px;font-weight:600;margin:20px 0;">
                    📍 Your Shared Location
                  </a>
                  <p style="font-size:13px;color:#6b7280;">
                    When you are safe, open the UnSocial app and click <strong>"I'm Safe — Resolve SOS"</strong> 
                    to notify your contacts.
                  </p>
                </div>
              </div>
            </body></html>
            """.formatted(userName, contactsNotified, mapsLink);
    }

    private String buildTimerExpiredHtml(String contactName, String userName, String note) {
        String noteSection = note != null
                ? "<p style='font-size:14px;color:#374151;'>Note they left: <em>\"" + note + "\"</em></p>"
                : "";
        return """
            <!DOCTYPE html><html><head><meta charset="UTF-8"/></head>
            <body style="margin:0;padding:0;background:#f3f4f6;font-family:Arial,sans-serif;">
              <div style="max-width:580px;margin:32px auto;background:#fff;border-radius:16px;overflow:hidden;">
                <div style="background:#d97706;padding:28px;text-align:center;">
                  <h1 style="color:#fff;font-size:20px;margin:0;">⏰ Safety Timer Expired</h1>
                </div>
                <div style="padding:28px;">
                  <p style="font-size:15px;color:#111827;">Hi <strong>%s</strong>,</p>
                  <p style="font-size:14px;color:#374151;line-height:1.6;">
                    <strong>%s</strong> set a safety timer and did not check in before it expired. 
                    Please reach out to them to confirm they are safe.
                  </p>
                  %s
                  <p style="font-size:13px;color:#6b7280;margin-top:16px;">
                    If you cannot reach them, consider checking on them in person or contacting emergency services.
                  </p>
                </div>
              </div>
            </body></html>
            """.formatted(contactName, userName, noteSection);
    }
}

package com.webtrans.Courier.service.notification;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Real email channel — sends via Mailtrap SMTP.
 * Also prints a console copy (backup for demos when SMTP is unavailable).
 *
 * This class replaces the old EmailService.
 */
@Component
public class EmailNotificationChannel implements NotificationChannel {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@courier.local}")
    private String fromEmail;

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }

    @Override
    public void send(NotificationRequest request) {

        // ===== ALWAYS: console copy (backup for demo) =====
        System.out.println("==============================================");
        System.out.println("  📧 EMAIL to: " + request.getRecipient());
        System.out.println("  Subject: " + request.getSubject());
        System.out.println("  Body: " + request.getBody());
        if (request.getParcel() != null) {
            System.out.println("  Track: http://localhost:8080/track.html?code="
                    + request.getParcel().getTrackingCode());
        }
        System.out.println("==============================================");

        // ===== TRY: real email via Mailtrap =====
        if (mailSender == null) {
            System.out.println("  ⚠️ MailSender not configured — console copy only.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(request.getRecipient());
            helper.setSubject(request.getSubject());

            String tracking = request.getParcel() != null
                    ? request.getParcel().getTrackingCode()
                    : "";

            String html = "<div style=\"font-family:Arial,sans-serif;padding:24px;max-width:520px;\">"
                    + "<h2 style=\"margin:0 0 12px;color:#0a0a0a;\">Your parcel is arriving</h2>"
                    + "<p style=\"color:#333;line-height:1.5;\">A delivery partner is at your location with parcel <b>"
                    + tracking + "</b>.</p>"
                    + "<p style=\"margin-top:20px;font-size:13px;color:#666;\">Your delivery code:</p>"
                    + "<div style=\"font-size:34px;font-weight:900;letter-spacing:8px;"
                    + "padding:16px 20px;background:#0a0a0a;color:#fff;display:inline-block;"
                    + "border-radius:12px;\">" + request.getOtpCode() + "</div>"
                    + "<p style=\"margin-top:20px;color:#666;font-size:13px;\">Show this code to the partner to receive your parcel.</p>"
                    + "<p style=\"margin-top:24px;\"><a href=\"http://localhost:8080/track.html?code="
                    + tracking + "\" style=\"color:#06c167;font-weight:700;\">Track your parcel live →</a></p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("  ✅ Real email sent to: " + request.getRecipient());

        } catch (Exception e) {
            System.out.println("  ⚠️ Real email failed (console copy still available): " + e.getMessage());
        }
    }
}
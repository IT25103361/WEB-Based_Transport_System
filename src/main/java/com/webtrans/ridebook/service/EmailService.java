package com.seproject.courier.service;

import com.seproject.courier.entity.Parcel;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@courier.local}")
    private String fromEmail;

    /**
     * Sends the delivery OTP to the receiver.
     * ALWAYS prints to console (backup for demo).
     * Also tries to send a real email via SMTP (Mailtrap) if configured.
     * Any email failure is caught and logged — never breaks the flow.
     */
    public void sendDeliveryOtp(Parcel parcel, String otpCode) {

        // ===== ALWAYS: print to console =====
        System.out.println("==============================================");
        System.out.println("  📧 EMAIL to: " + parcel.getReceiverEmail());
        System.out.println("  Subject: Your delivery code for " + parcel.getTrackingCode());
        System.out.println("  Body:");
        System.out.println("    Your delivery code is " + otpCode + ".");
        System.out.println("    Show it to the delivery partner when they arrive.");
        System.out.println("    Track: http://localhost:8080/track.html?code=" + parcel.getTrackingCode());
        System.out.println("==============================================");

        // ===== TRY: send real email via SMTP =====
        if (mailSender == null) {
            System.out.println("  ⚠️ MailSender not configured — console copy only.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(parcel.getReceiverEmail());
            helper.setSubject("Your delivery code for " + parcel.getTrackingCode());

            String html = "<div style=\"font-family:Arial,sans-serif;padding:24px;max-width:520px;\">"
                    + "<h2 style=\"margin:0 0 12px;color:#0a0a0a;\">Your parcel is arriving</h2>"
                    + "<p style=\"color:#333;line-height:1.5;\">A delivery partner is at your location with parcel <b>"
                    + parcel.getTrackingCode() + "</b>.</p>"
                    + "<p style=\"margin-top:20px;font-size:13px;color:#666;\">Your delivery code:</p>"
                    + "<div style=\"font-size:34px;font-weight:900;letter-spacing:8px;"
                    + "padding:16px 20px;background:#0a0a0a;color:#fff;display:inline-block;"
                    + "border-radius:12px;\">" + otpCode + "</div>"
                    + "<p style=\"margin-top:20px;color:#666;font-size:13px;\">Show this code to the partner to receive your parcel.</p>"
                    + "<p style=\"margin-top:24px;\"><a href=\"http://localhost:8080/track.html?code="
                    + parcel.getTrackingCode() + "\" style=\"color:#06c167;font-weight:700;\">Track your parcel live →</a></p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("  ✅ Real email sent to: " + parcel.getReceiverEmail());

        } catch (Exception e) {
            System.out.println("  ⚠️ Real email failed (console copy still available): " + e.getMessage());
            // Intentionally swallowed — do not break delivery flow if email fails
        }
    }
}
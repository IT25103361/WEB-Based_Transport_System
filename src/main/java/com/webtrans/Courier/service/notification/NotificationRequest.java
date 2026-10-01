package com.webtrans.Courier.service.notification;

import com.webtrans.Courier.model.Parcel;
import lombok.*;

/**
 * Immutable payload carrying everything a channel needs to send a notification.
 * Created via static factory methods to keep call sites readable.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    private String recipient;
    private String subject;
    private String body;

    private Parcel parcel;      // optional — used for tracking link
    private String otpCode;     // optional — used for HTML formatting

    /** For OTP delivery notifications. */
    public static NotificationRequest ofDeliveryOtp(Parcel parcel, String otpCode) {
        return NotificationRequest.builder()
                .recipient(parcel.getReceiverEmail())
                .subject("Your delivery code for " + parcel.getTrackingCode())
                .body("Your delivery code is " + otpCode +
                        ". Show it to the delivery partner when they arrive.")
                .parcel(parcel)
                .otpCode(otpCode)
                .build();
    }
}
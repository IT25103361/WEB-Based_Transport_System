package com.seproject.courier.dto.parcel;

import com.seproject.courier.entity.Parcel;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelResponse {

    private Long id;
    private String trackingCode;
    private Long senderId;
    private String senderName;

    private String receiverName;
    private String receiverEmail;
    private String receiverPhone;

    private String pickupAddress;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;

    private String dropAddress;
    private BigDecimal dropLat;
    private BigDecimal dropLng;

    private BigDecimal weightKg;
    private Parcel.Size size;
    private Parcel.ServiceTier serviceTier;
    private Parcel.Status status;
    private BigDecimal price;

    // Cancellation info (shown if CANCELLED / FAILED)
    private String cancelReason;
    private String cancelledBy;
    private boolean refundEligible;   // true when a partner cancelled after already picking it up

    // Delivery OTP — only populated when the parcel is ARRIVED and the code is still valid.
    private String deliveryOtp;
    private LocalDateTime otpExpiresAt;

    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
}
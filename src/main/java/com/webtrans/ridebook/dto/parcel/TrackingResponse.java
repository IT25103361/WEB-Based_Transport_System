package com.seproject.courier.dto.parcel;

import com.seproject.courier.entity.Parcel;
import lombok.*;
import com.seproject.courier.entity.Partner;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingResponse {

    private Long parcelId;
    private String trackingCode;
    private Parcel.Status status;
    private String statusMessage;

    private String pickupAddress;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;

    private String dropAddress;
    private BigDecimal dropLat;
    private BigDecimal dropLng;

    private String receiverFirstName;

    private Parcel.ServiceTier serviceTier;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime expectedDelivery;

    private Long partnerId;
    private String partnerName;
    private Partner.VehicleType partnerVehicleType;
    private BigDecimal partnerLat;
    private BigDecimal partnerLng;

    // Cancellation info (shown if CANCELLED / FAILED)
    private String cancelReason;
    private String cancelledBy;

    // OTP — only populated when status is ARRIVED and not yet verified
    private String deliveryOtp;
    private LocalDateTime otpExpiresAt;

    private List<StatusHistoryResponse> timeline;
}
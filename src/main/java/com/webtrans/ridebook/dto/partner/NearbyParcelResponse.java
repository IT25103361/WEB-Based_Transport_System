package com.seproject.courier.dto.partner;

import com.seproject.courier.entity.Parcel;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyParcelResponse {

    private Long parcelId;
    private String trackingCode;

    // Sender info — display only, no email exposed
    private String senderName;

    // Pickup
    private String pickupAddress;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;

    // Drop
    private String dropAddress;
    private BigDecimal dropLat;
    private BigDecimal dropLng;

    private BigDecimal weightKg;
    private Parcel.Size size;
    private Parcel.ServiceTier serviceTier;
    private BigDecimal price;

    // Distance from partner's current location to pickup (km)
    private double distanceKm;
}
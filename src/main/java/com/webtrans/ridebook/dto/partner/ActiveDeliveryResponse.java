package com.seproject.courier.dto.partner;

import com.seproject.courier.entity.Parcel;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveDeliveryResponse {

    private Long assignmentId;
    private Long parcelId;
    private String trackingCode;
    private Parcel.Status status;

    private String senderName;
    private String receiverName;
    private String receiverPhone;

    private String pickupAddress;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;

    private String dropAddress;
    private BigDecimal dropLat;
    private BigDecimal dropLng;

    private BigDecimal price;
    private boolean otpRequired;
}

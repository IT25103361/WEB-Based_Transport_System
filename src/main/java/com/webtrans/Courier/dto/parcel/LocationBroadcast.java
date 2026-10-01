package com.webtrans.Courier.dto.parcel;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationBroadcast {

    private Long parcelId;
    private String trackingCode;
    private Long partnerId;
    private String partnerName;
    private BigDecimal lat;
    private BigDecimal lng;
    private LocalDateTime timestamp;
}
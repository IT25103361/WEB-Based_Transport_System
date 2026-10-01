package com.webtrans.Courier.service.observer;

import com.webtrans.Courier.model.Parcel;
import com.webtrans.Courier.model.User;
import lombok.*;

import java.math.BigDecimal;

/**
 * Event published whenever a parcel changes status.
 * Carries everything observers need — so they don't have to fetch anything.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelStatusEvent {

    private Parcel parcel;
    private Parcel.Status oldStatus;
    private Parcel.Status newStatus;
    private User changedBy;     // who triggered the change (partner, sender, system)
    private String note;        // human-readable description
    private BigDecimal lat;     // location where change happened (optional)
    private BigDecimal lng;
}
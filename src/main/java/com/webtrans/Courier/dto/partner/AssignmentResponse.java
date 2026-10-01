package com.webtrans.Courier.dto.partner;

import com.webtrans.Courier.model.Parcel;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentResponse {

    private Long assignmentId;
    private Long parcelId;
    private String trackingCode;
    private Parcel.Status parcelStatus;

    private Long partnerId;
    private String partnerName;

    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
}
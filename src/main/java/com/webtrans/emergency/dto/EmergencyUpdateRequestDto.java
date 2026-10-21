package com.webtrans.emergency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyUpdateRequestDto {

    private String trackingToken;

    private String managementToken;

    private String locationAddress;

    private String locationLandmark;

    private String incidentDescription;

    private Double latitude;

    private Double longitude;

    private String csrfToken;
}

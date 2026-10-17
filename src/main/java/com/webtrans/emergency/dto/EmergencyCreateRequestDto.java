package com.webtrans.emergency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyCreateRequestDto {

    private List<String> services;

    private Integer agentCount;

    private Double latitude;

    private Double longitude;

    private String locationAddress;

    private String locationLandmark;

    private String contactName;

    private String callbackPhone;

    private String incidentDescription;

    private String idempotencyKey;

    private String csrfToken;
}

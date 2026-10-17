package com.webtrans.emergency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAdminRequestDto {

    private Long id;

    private String trackingToken;

    private List<String> requestedServices;

    private Integer requestedAgentCount;

    private Integer assignedAgentsCount;

    private Double latitude;

    private Double longitude;

    private String locationAddress;

    private String locationLandmark;

    private String contactName;

    private String callbackPhone;

    private String incidentDescription;

    private String status;

    private String clientIp;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @com.fasterxml.jackson.annotation.JsonProperty("isDeleted")
    private boolean isDeleted;

    private LocalDateTime deletedAt;

    private String deletionReason;

    private String deletionNote;

    private String deletedByActorType;

    private List<EmergencyTrackingResponseDto.StatusHistoryItemDto> history;
}

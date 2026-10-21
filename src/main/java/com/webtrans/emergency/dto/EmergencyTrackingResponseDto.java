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
public class EmergencyTrackingResponseDto {

    private String trackingToken;

    private List<String> requestedServices;

    private Integer requestedAgentCount;

    private Integer assignedAgentsCount;

    private String status;

    private String statusDisplayName;

    private Double latitude;

    private Double longitude;

    private String locationSummary;

    private String locationAddress;

    private String locationLandmark;

    private String incidentDescription;

    private String contactName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String systemNotice;

    private boolean demonstrationMode;

    @com.fasterxml.jackson.annotation.JsonProperty("isDeleted")
    private boolean isDeleted;

    private String managementToken;

    private boolean canDelete;

    private List<StatusHistoryItemDto> history;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusHistoryItemDto {
        private String status;
        private String note;
        private LocalDateTime timestamp;
    }
}

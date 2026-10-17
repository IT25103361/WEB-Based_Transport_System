package com.webtrans.emergency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyDeleteRequestDto {

    private String trackingToken;

    private String managementToken;

    private String reason;

    private String note;

    private String csrfToken;
}

package com.webtrans.emergency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAdminUpdateDto {

    private String status;

    private Integer assignedAgents;

    private String note;
}

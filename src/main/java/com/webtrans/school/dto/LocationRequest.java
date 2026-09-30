package com.webtrans.school.dto;

import jakarta.validation.constraints.*;

public record LocationRequest(
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @DecimalMin("0.0") Double speed,
        Double heading,
        String gpsStatus
) {}
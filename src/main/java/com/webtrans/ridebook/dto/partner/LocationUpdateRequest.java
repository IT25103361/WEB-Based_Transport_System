package com.seproject.courier.dto.partner;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateRequest {

    @NotNull(message = "Latitude is required")
    private BigDecimal lat;

    @NotNull(message = "Longitude is required")
    private BigDecimal lng;
}
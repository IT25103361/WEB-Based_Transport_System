package com.seproject.courier.dto.partner;

import com.seproject.courier.entity.Partner;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;

    private Partner.VehicleType vehicleType;
    private Boolean isOnline;
    private Boolean isAvailable;
    private BigDecimal currentLat;
    private BigDecimal currentLng;
    private BigDecimal rating;
}
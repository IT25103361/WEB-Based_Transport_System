package com.webtrans.Courier.dto.partner;

import com.webtrans.Courier.model.Partner;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePartnerRequest {

    @NotNull(message = "Vehicle type is required")
    private Partner.VehicleType vehicleType;
}
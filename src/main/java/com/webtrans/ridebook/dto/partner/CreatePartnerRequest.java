package com.seproject.courier.dto.partner;

import com.seproject.courier.entity.Partner;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePartnerRequest {

    @NotNull(message = "Vehicle type is required")
    private Partner.VehicleType vehicleType;
}
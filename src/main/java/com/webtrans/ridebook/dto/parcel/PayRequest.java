package com.seproject.courier.dto.parcel;

import com.seproject.courier.entity.Payment;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayRequest {

    @NotNull(message = "Payment method is required")
    private Payment.Method method;
}
package com.webtrans.Courier.dto.parcel;

import com.webtrans.Courier.model.Payment;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayRequest {

    @NotNull(message = "Payment method is required")
    private Payment.Method method;
}
package com.webtrans.Courier.dto.parcel;

import com.webtrans.Courier.model.Payment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private Long parcelId;
    private String trackingCode;
    private BigDecimal amount;
    private Payment.Method method;
    private Payment.Status status;
    private String txnRef;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
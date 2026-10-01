package com.webtrans.Courier.dto.parcel;

import com.webtrans.Courier.model.Parcel;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistoryResponse {

    private Long id;
    private Parcel.Status status;
    private String note;
    private BigDecimal lat;
    private BigDecimal lng;
    private String changedByName;
    private LocalDateTime changedAt;
}
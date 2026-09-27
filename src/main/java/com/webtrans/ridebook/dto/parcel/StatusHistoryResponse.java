package com.seproject.courier.dto.parcel;

import com.seproject.courier.entity.Parcel;
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
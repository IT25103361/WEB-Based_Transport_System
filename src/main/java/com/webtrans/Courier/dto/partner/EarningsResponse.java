package com.webtrans.Courier.dto.partner;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarningsResponse {

    private BigDecimal totalEarned;
    private BigDecimal thisMonthEarned;
    private int totalDeliveries;
    private int thisMonthDeliveries;

    private List<MonthlyBucket> monthly;   // newest month first
    private List<DeliveryLine> recent;     // last 20 completed jobs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyBucket {
        private String month;          // "2026-09"
        private String label;          // "Sep 2026"
        private BigDecimal amount;
        private int deliveries;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryLine {
        private Long parcelId;
        private String trackingCode;
        private String dropAddress;
        private BigDecimal amount;
        private LocalDateTime deliveredAt;
    }
}
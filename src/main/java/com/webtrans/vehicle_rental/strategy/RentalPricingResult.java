package com.webtrans.vehicle_rental.strategy;

import java.math.BigDecimal;

/**
 * Encapsulates the pricing breakdown calculated for a new rental booking.
 */
public class RentalPricingResult {

    private final int billableDays;
    private final BigDecimal dailyRate;
    private final BigDecimal subtotal;
    private final BigDecimal totalCost;
    private final String explanation;

    public RentalPricingResult(int billableDays, BigDecimal dailyRate, BigDecimal subtotal, BigDecimal totalCost, String explanation) {
        this.billableDays = billableDays;
        this.dailyRate = dailyRate;
        this.subtotal = subtotal;
        this.totalCost = totalCost;
        this.explanation = explanation;
    }

    public int getBillableDays() {
        return billableDays;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public String getExplanation() {
        return explanation;
    }
}

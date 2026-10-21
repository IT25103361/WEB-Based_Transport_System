package com.webtrans.vehicle_rental.strategy;

import java.math.BigDecimal;

/**
 * Encapsulates the pricing breakdown calculated for a rental extension.
 */
public class RentalExtensionPricingResult {

    private final int additionalDays;
    private final BigDecimal dailyRate;
    private final BigDecimal additionalCost;
    private final BigDecimal previousCost;
    private final BigDecimal newTotalCost;
    private final int newTotalDays;

    public RentalExtensionPricingResult(int additionalDays, BigDecimal dailyRate, BigDecimal additionalCost,
                                        BigDecimal previousCost, BigDecimal newTotalCost, int newTotalDays) {
        this.additionalDays = additionalDays;
        this.dailyRate = dailyRate;
        this.additionalCost = additionalCost;
        this.previousCost = previousCost;
        this.newTotalCost = newTotalCost;
        this.newTotalDays = newTotalDays;
    }

    public int getAdditionalDays() {
        return additionalDays;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public BigDecimal getAdditionalCost() {
        return additionalCost;
    }

    public BigDecimal getPreviousCost() {
        return previousCost;
    }

    public BigDecimal getNewTotalCost() {
        return newTotalCost;
    }

    public int getNewTotalDays() {
        return newTotalDays;
    }
}

package com.webtrans.vehicle_rental.strategy;

import com.webtrans.vehicle_rental.model.RentalVehicle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Concrete strategy implementing pricing calculation for new rental bookings.
 * Encapsulates the 24-hour cycle billable days rule, daily rates, and rounding.
 */
@Component
public class NewRentalPricingStrategy implements RentalPricingStrategy {

    @Override
    public String getStrategyName() {
        return "NEW_RENTAL";
    }

    /**
     * Billable days rule:
     * - Calculated as 24-hour periods from Start Date to End Date.
     * - If Start Date == End Date, minimum 1 billable day.
     * - If End Date > Start Date, billable days = DAYS.between(start, end).
     */
    public int calculateBillableDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Return date cannot be earlier than start date.");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return days < 1 ? 1 : (int) days;
    }

    public RentalPricingResult calculatePrice(RentalVehicle vehicle, LocalDate startDate, LocalDate endDate) {
        int billableDays = calculateBillableDays(startDate, endDate);
        double rate = (vehicle != null && vehicle.getDailyRate() != null) ? vehicle.getDailyRate() : 0.0;
        BigDecimal dailyRate = BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = dailyRate.multiply(BigDecimal.valueOf(billableDays)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal;

        String explanation = billableDays + " billable day" + (billableDays == 1 ? "" : "s") +
                " (24-hour daily cycle from " + startDate + " to " + endDate + ").";

        return new RentalPricingResult(billableDays, dailyRate, subtotal, total, explanation);
    }
}

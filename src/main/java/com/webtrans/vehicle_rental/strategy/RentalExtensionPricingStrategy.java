package com.webtrans.vehicle_rental.strategy;

import com.webtrans.vehicle_rental.model.RentalBooking;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Concrete strategy implementing pricing calculation for rental extensions.
 * Computes additional billable days, incremental cost, and updated total amounts.
 */
@Component
public class RentalExtensionPricingStrategy implements RentalPricingStrategy {

    @Override
    public String getStrategyName() {
        return "RENTAL_EXTENSION";
    }

    public RentalExtensionPricingResult calculateExtensionPrice(RentalBooking booking, LocalDate newEndDate) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking must not be null.");
        }
        if (newEndDate == null || !newEndDate.isAfter(booking.getEndDate())) {
            throw new IllegalArgumentException("New return date must be strictly after current return date (" + booking.getEndDate() + ").");
        }

        int additionalDays = (int) ChronoUnit.DAYS.between(booking.getEndDate(), newEndDate);
        double rate = booking.getDailyRate() != null ? booking.getDailyRate() :
                (booking.getVehicle() != null && booking.getVehicle().getDailyRate() != null ? booking.getVehicle().getDailyRate() : 0.0);
        BigDecimal dailyRate = BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal additionalCost = dailyRate.multiply(BigDecimal.valueOf(additionalDays)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal prevCost = BigDecimal.valueOf(booking.getTotalCost() != null ? booking.getTotalCost() : 0.0)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal newTotalCost = prevCost.add(additionalCost).setScale(2, RoundingMode.HALF_UP);
        int newTotalDays = (booking.getDays() != null ? booking.getDays() : 0) + additionalDays;

        return new RentalExtensionPricingResult(additionalDays, dailyRate, additionalCost, prevCost, newTotalCost, newTotalDays);
    }
}

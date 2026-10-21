package com.webtrans.vehicle_rental.strategy;

import com.webtrans.vehicle_rental.model.RentalBooking;
import com.webtrans.vehicle_rental.model.RentalVehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Context class for the Vehicle Rental Strategy Pattern.
 * Coordinates execution of NewRentalPricingStrategy and RentalExtensionPricingStrategy
 * across quotation and booking confirmation workflows.
 */
@Service
public class RentalPriceCalculator {

    private final NewRentalPricingStrategy newRentalPricingStrategy;
    private final RentalExtensionPricingStrategy rentalExtensionPricingStrategy;

    @Autowired
    public RentalPriceCalculator(NewRentalPricingStrategy newRentalPricingStrategy,
                                 RentalExtensionPricingStrategy rentalExtensionPricingStrategy) {
        this.newRentalPricingStrategy = newRentalPricingStrategy;
        this.rentalExtensionPricingStrategy = rentalExtensionPricingStrategy;
    }

    public RentalPricingResult calculateNewRentalPrice(RentalVehicle vehicle, LocalDate startDate, LocalDate endDate) {
        return newRentalPricingStrategy.calculatePrice(vehicle, startDate, endDate);
    }

    public RentalExtensionPricingResult calculateExtensionPrice(RentalBooking booking, LocalDate newEndDate) {
        return rentalExtensionPricingStrategy.calculateExtensionPrice(booking, newEndDate);
    }

    public int calculateBillableDays(LocalDate startDate, LocalDate endDate) {
        return newRentalPricingStrategy.calculateBillableDays(startDate, endDate);
    }

    public NewRentalPricingStrategy getNewRentalPricingStrategy() {
        return newRentalPricingStrategy;
    }

    public RentalExtensionPricingStrategy getRentalExtensionPricingStrategy() {
        return rentalExtensionPricingStrategy;
    }
}

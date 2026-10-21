package com.webtrans;

import com.webtrans.vehicle_rental.model.RentalBooking;
import com.webtrans.vehicle_rental.model.RentalVehicle;
import com.webtrans.vehicle_rental.strategy.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RentalPricingStrategyTest {

    @Autowired
    private RentalPriceCalculator priceCalculator;

    @Autowired
    private NewRentalPricingStrategy newRentalPricingStrategy;

    @Autowired
    private RentalExtensionPricingStrategy rentalExtensionPricingStrategy;

    @Test
    @DisplayName("1. NewRentalPricingStrategy: Same-day booking yields 1 billable day and correct price")
    void testNewRentalSameDay() {
        RentalVehicle vehicle = new RentalVehicle();
        vehicle.setId(10L);
        vehicle.setName("Toyota Prius");
        vehicle.setDailyRate(5000.0);

        LocalDate date = LocalDate.now().plusDays(5);
        RentalPricingResult result = newRentalPricingStrategy.calculatePrice(vehicle, date, date);

        assertEquals(1, result.getBillableDays());
        assertEquals(new BigDecimal("5000.00"), result.getDailyRate());
        assertEquals(new BigDecimal("5000.00"), result.getSubtotal());
        assertEquals(new BigDecimal("5000.00"), result.getTotalCost());
        assertTrue(result.getExplanation().contains("1 billable day"));
    }

    @Test
    @DisplayName("2. NewRentalPricingStrategy: Multi-day booking calculates 24-hr daily cycles")
    void testNewRentalMultiDay() {
        RentalVehicle vehicle = new RentalVehicle();
        vehicle.setId(11L);
        vehicle.setName("Honda Vezel");
        vehicle.setDailyRate(7500.0);

        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = start.plusDays(4); // 4 days

        RentalPricingResult result = priceCalculator.calculateNewRentalPrice(vehicle, start, end);

        assertEquals(4, result.getBillableDays());
        assertEquals(new BigDecimal("7500.00"), result.getDailyRate());
        assertEquals(new BigDecimal("30000.00"), result.getSubtotal());
        assertEquals(new BigDecimal("30000.00"), result.getTotalCost());
    }

    @Test
    @DisplayName("3. RentalExtensionPricingStrategy: Calculates incremental cost and new total cost")
    void testRentalExtensionPricing() {
        RentalVehicle vehicle = new RentalVehicle();
        vehicle.setId(12L);
        vehicle.setName("Suzuki WagonR");
        vehicle.setDailyRate(4000.0);

        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate oldEndDate = startDate.plusDays(3); // 3 days initially
        LocalDate newEndDate = oldEndDate.plusDays(2); // extend by 2 days

        RentalBooking booking = new RentalBooking();
        booking.setId(99L);
        booking.setVehicle(vehicle);
        booking.setStartDate(startDate);
        booking.setEndDate(oldEndDate);
        booking.setDays(3);
        booking.setDailyRate(4000.0);
        booking.setTotalCost(12000.0);

        RentalExtensionPricingResult result = priceCalculator.calculateExtensionPrice(booking, newEndDate);

        assertEquals(2, result.getAdditionalDays());
        assertEquals(new BigDecimal("4000.00"), result.getDailyRate());
        assertEquals(new BigDecimal("8000.00"), result.getAdditionalCost());
        assertEquals(new BigDecimal("12000.00"), result.getPreviousCost());
        assertEquals(new BigDecimal("20000.00"), result.getNewTotalCost());
        assertEquals(5, result.getNewTotalDays());
    }

    @Test
    @DisplayName("4. RentalExtensionPricingStrategy: Invalid new end date strictly rejected")
    void testInvalidExtensionDate() {
        RentalBooking booking = new RentalBooking();
        booking.setEndDate(LocalDate.now().plusDays(5));

        assertThrows(IllegalArgumentException.class, () ->
                rentalExtensionPricingStrategy.calculateExtensionPrice(booking, LocalDate.now().plusDays(4))
        );
    }
}

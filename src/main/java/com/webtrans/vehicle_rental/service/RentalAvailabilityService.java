package com.webtrans.vehicle_rental.service;

import com.webtrans.auth.model.User;
import com.webtrans.vehicle_rental.dto.RentalExtensionQuoteDto;
import com.webtrans.vehicle_rental.dto.RentalQuoteDto;
import com.webtrans.vehicle_rental.model.RentalBooking;
import com.webtrans.vehicle_rental.model.RentalVehicle;
import com.webtrans.vehicle_rental.repository.RentalBookingRepository;
import com.webtrans.vehicle_rental.repository.RentalVehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class RentalAvailabilityService {

    // Statuses that actively hold and block vehicle dates.
    // CANCELLED and COMPLETED bookings immediately release dates.
    public static final List<String> ACTIVE_BLOCKING_STATUSES = List.of("PENDING", "CONFIRMED");

    @Autowired
    private RentalVehicleRepository vehicleRepository;

    @Autowired
    private RentalBookingRepository bookingRepository;

    @Autowired
    private com.webtrans.vehicle_rental.strategy.RentalPriceCalculator priceCalculator;

    /**
     * Billable days rule:
     * - Calculated as 24-hour periods from Start Date to End Date.
     * - If Start Date == End Date, minimum 1 billable day.
     * - If End Date > Start Date, billable days = DAYS.between(start, end).
     */
    public int calculateBillableDays(LocalDate startDate, LocalDate endDate) {
        return priceCalculator.calculateBillableDays(startDate, endDate);
    }

    /**
     * Effective end date for boundary comparison:
     * If start == end (same-day booking), it occupies [start, start + 1 day).
     * If start < end, it occupies [start, end).
     */
    public LocalDate getEffectiveEndDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return endDate;
        return startDate.equals(endDate) ? startDate.plusDays(1) : endDate;
    }

    /**
     * Overlap rule:
     * Two bookings [S1, E1_eff) and [S2, E2_eff) overlap if and only if:
     * S1 < E2_eff && S2 < E1_eff
     */
    public boolean isOverlapping(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        if (startA == null || endA == null || startB == null || endB == null) return false;
        LocalDate effA = getEffectiveEndDate(startA, endA);
        LocalDate effB = getEffectiveEndDate(startB, endB);
        return startA.isBefore(effB) && startB.isBefore(effA);
    }

    /**
     * Checks if a vehicle is available for a given date range.
     * Optionally excludes a specific booking ID (used for extensions).
     */
    @Transactional(readOnly = true)
    public AvailabilityResult checkVehicleAvailability(Long vehicleId, LocalDate startDate, LocalDate endDate, Long excludeBookingId) {
        if (vehicleId == null) {
            return new AvailabilityResult(false, "No vehicle selected.", null);
        }
        if (startDate == null || endDate == null) {
            return new AvailabilityResult(false, "Start and end dates are required.", null);
        }
        if (startDate.isBefore(LocalDate.now())) {
            return new AvailabilityResult(false, "Rental start date cannot be in the past.", null);
        }
        if (endDate.isBefore(startDate)) {
            return new AvailabilityResult(false, "Rental return date cannot be before start date.", null);
        }

        RentalVehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle == null) {
            return new AvailabilityResult(false, "Selected vehicle was not found.", null);
        }

        // Check if vehicle itself is disabled or under maintenance
        String status = vehicle.getStatus() != null ? vehicle.getStatus().toUpperCase() : "AVAILABLE";
        if ("MAINTENANCE".equals(status) || "INACTIVE".equals(status)) {
            return new AvailabilityResult(false, "Vehicle is currently under maintenance or inactive.", null);
        }

        List<RentalBooking> activeBookings;
        if (excludeBookingId != null) {
            activeBookings = bookingRepository.findByVehicle_IdAndIdNotAndStatusIn(vehicleId, excludeBookingId, ACTIVE_BLOCKING_STATUSES);
        } else {
            activeBookings = bookingRepository.findByVehicle_IdAndStatusIn(vehicleId, ACTIVE_BLOCKING_STATUSES);
        }

        for (RentalBooking existing : activeBookings) {
            if (isOverlapping(startDate, endDate, existing.getStartDate(), existing.getEndDate())) {
                String conflictMsg = "Already reserved from " + existing.getStartDate() + " to " + existing.getEndDate() +
                        " (" + existing.getStatus() + ").";
                return new AvailabilityResult(false, conflictMsg, existing);
            }
        }

        return new AvailabilityResult(true, "Vehicle is available for the requested period.", null);
    }

    /**
     * Builds authoritative price quote with BigDecimal calculations.
     */
    @Transactional(readOnly = true)
    public RentalQuoteDto calculateQuote(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        if (vehicleId == null || startDate == null || endDate == null) {
            return new RentalQuoteDto();
        }

        RentalVehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle == null) {
            return new RentalQuoteDto();
        }

        AvailabilityResult avail = checkVehicleAvailability(vehicleId, startDate, endDate, null);
        com.webtrans.vehicle_rental.strategy.RentalPricingResult pricing;
        try {
            pricing = priceCalculator.calculateNewRentalPrice(vehicle, startDate, endDate);
        } catch (IllegalArgumentException e) {
            pricing = new com.webtrans.vehicle_rental.strategy.RentalPricingResult(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, e.getMessage());
        }

        return new RentalQuoteDto(
                vehicle.getId(),
                vehicle.getName(),
                vehicle.getType(),
                pricing.getDailyRate(),
                startDate,
                endDate,
                pricing.getBillableDays(),
                pricing.getSubtotal(),
                pricing.getTotalCost(),
                avail.isAvailable(),
                avail.getMessage(),
                pricing.getExplanation()
        );
    }

    /**
     * Fetches all booked date ranges for a vehicle (for date picker display).
     */
    @Transactional(readOnly = true)
    public List<Map<String, String>> getBookedRangesForVehicle(Long vehicleId) {
        List<RentalBooking> bookings = bookingRepository.findByVehicle_IdAndStatusIn(vehicleId, ACTIVE_BLOCKING_STATUSES);
        List<Map<String, String>> ranges = new ArrayList<>();
        for (RentalBooking b : bookings) {
            Map<String, String> range = new HashMap<>();
            range.put("start", b.getStartDate().toString());
            range.put("end", b.getEndDate().toString());
            range.put("status", b.getStatus());
            ranges.add(range);
        }
        return ranges;
    }

    /**
     * Atomically creates a rental booking with pessimistic write lock on vehicle row.
     */
    @Transactional
    public RentalBooking createBookingAtomic(Long vehicleId, User customerUser, String customerName,
                                            String customerPhone, LocalDate startDate, LocalDate endDate) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("Vehicle ID is required.");
        }
        if (customerUser == null) {
            throw new IllegalStateException("Authentication required to create a booking.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and return date are required.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Rental start date cannot be in the past.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Return date cannot be earlier than start date.");
        }

        // Acquire row-level pessimistic write lock on the vehicle record.
        RentalVehicle vehicle = vehicleRepository.findByIdWithLock(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Selected vehicle does not exist."));

        String vStatus = vehicle.getStatus() != null ? vehicle.getStatus().toUpperCase() : "AVAILABLE";
        if ("MAINTENANCE".equals(vStatus) || "INACTIVE".equals(vStatus)) {
            throw new IllegalStateException("Vehicle is currently under maintenance and cannot be booked.");
        }

        // Re-check overlap inside the locked transaction
        List<RentalBooking> activeBookings = bookingRepository.findByVehicle_IdAndStatusIn(vehicleId, ACTIVE_BLOCKING_STATUSES);
        for (RentalBooking existing : activeBookings) {
            if (isOverlapping(startDate, endDate, existing.getStartDate(), existing.getEndDate())) {
                throw new IllegalStateException("Vehicle was reserved by another customer for overlapping dates (" +
                        existing.getStartDate() + " to " + existing.getEndDate() + "). Please choose other dates.");
            }
        }

        // Calculate authoritative price on server using Strategy Pattern
        com.webtrans.vehicle_rental.strategy.RentalPricingResult pricing = priceCalculator.calculateNewRentalPrice(vehicle, startDate, endDate);

        RentalBooking booking = new RentalBooking();
        booking.setVehicle(vehicle);
        booking.setCustomerUser(customerUser);
        booking.setCustomerName((customerName != null && !customerName.isBlank()) ? customerName.trim() : customerUser.getFullName());
        booking.setCustomerPhone((customerPhone != null && !customerPhone.isBlank()) ? customerPhone.trim() : "0771234567");
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setDays(pricing.getBillableDays());
        booking.setDailyRate(pricing.getDailyRate().doubleValue());
        booking.setTotalCost(pricing.getTotalCost().doubleValue());
        booking.setStatus("PENDING");

        return bookingRepository.save(booking);
    }

    /**
     * Previews rental extension with price quote and conflict check.
     */
    @Transactional(readOnly = true)
    public RentalExtensionQuoteDto previewExtension(Long bookingId, User customerUser, LocalDate newEndDate) {
        RentalBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (booking.getCustomerUser() != null && !booking.getCustomerUser().getId().equals(customerUser.getId())) {
            throw new SecurityException("Access denied: You do not own this booking.");
        }

        if (!"PENDING".equalsIgnoreCase(booking.getStatus()) && !"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            return new RentalExtensionQuoteDto(booking.getId(), booking.getVehicle().getName(),
                    booking.getEndDate(), newEndDate, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.valueOf(booking.getTotalCost() != null ? booking.getTotalCost() : 0.0),
                    BigDecimal.valueOf(booking.getTotalCost() != null ? booking.getTotalCost() : 0.0),
                    false, "Only active or confirmed bookings can be extended (Current status: " + booking.getStatus() + ").");
        }

        if (newEndDate == null || !newEndDate.isAfter(booking.getEndDate())) {
            return new RentalExtensionQuoteDto(booking.getId(), booking.getVehicle().getName(),
                    booking.getEndDate(), newEndDate, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.valueOf(booking.getTotalCost() != null ? booking.getTotalCost() : 0.0),
                    BigDecimal.valueOf(booking.getTotalCost() != null ? booking.getTotalCost() : 0.0),
                    false, "New return date must be strictly after current return date (" + booking.getEndDate() + ").");
        }

        // Calculate extension price using RentalExtensionPricingStrategy
        com.webtrans.vehicle_rental.strategy.RentalExtensionPricingResult extPricing =
                priceCalculator.calculateExtensionPrice(booking, newEndDate);

        // Check if extension range [oldEndDate, newEndDate] conflicts with another booking for that vehicle
        AvailabilityResult avail = checkVehicleAvailability(booking.getVehicle().getId(), booking.getEndDate(), newEndDate, booking.getId());

        String message = avail.isAvailable() ?
                "Extension available! +" + extPricing.getAdditionalDays() + " day(s) for LKR " + extPricing.getAdditionalCost() + "." :
                "Extension unavailable: " + avail.getMessage();

        return new RentalExtensionQuoteDto(
                booking.getId(),
                booking.getVehicle().getName(),
                booking.getEndDate(),
                newEndDate,
                extPricing.getAdditionalDays(),
                extPricing.getDailyRate(),
                extPricing.getAdditionalCost(),
                extPricing.getPreviousCost(),
                extPricing.getNewTotalCost(),
                avail.isAvailable(),
                message
        );
    }

    /**
     * Atomically executes rental extension with pessimistic lock.
     */
    @Transactional
    public RentalBooking extendBookingAtomic(Long bookingId, User customerUser, LocalDate newEndDate) {
        RentalBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (booking.getCustomerUser() != null && !booking.getCustomerUser().getId().equals(customerUser.getId())) {
            throw new SecurityException("Access denied: You do not own this booking.");
        }

        if (!"PENDING".equalsIgnoreCase(booking.getStatus()) && !"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Only active or confirmed bookings can be extended (Current status: " + booking.getStatus() + ").");
        }

        if (newEndDate == null || !newEndDate.isAfter(booking.getEndDate())) {
            throw new IllegalArgumentException("New return date must be strictly after current return date (" + booking.getEndDate() + ").");
        }

        // Lock vehicle
        RentalVehicle vehicle = vehicleRepository.findByIdWithLock(booking.getVehicle().getId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle record not found."));

        // Check availability for extension window [booking.endDate, newEndDate], excluding current booking
        List<RentalBooking> otherBookings = bookingRepository.findByVehicle_IdAndIdNotAndStatusIn(
                vehicle.getId(), booking.getId(), ACTIVE_BLOCKING_STATUSES);

        for (RentalBooking other : otherBookings) {
            if (isOverlapping(booking.getEndDate(), newEndDate, other.getStartDate(), other.getEndDate())) {
                throw new IllegalStateException("Cannot extend: Vehicle is already reserved by another customer from " +
                        other.getStartDate() + " to " + other.getEndDate() + ".");
            }
        }

        // Calculate extension pricing using RentalExtensionPricingStrategy
        com.webtrans.vehicle_rental.strategy.RentalExtensionPricingResult extPricing =
                priceCalculator.calculateExtensionPrice(booking, newEndDate);

        // Traceability audit fields
        booking.setExtendedFromDate(booking.getEndDate());
        booking.setEndDate(newEndDate);
        booking.setDays(extPricing.getNewTotalDays());
        booking.setDailyRate(extPricing.getDailyRate().doubleValue());
        booking.setExtensionCost(extPricing.getAdditionalCost().doubleValue());
        booking.setTotalCost(extPricing.getNewTotalCost().doubleValue());
        booking.setExtendedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    public static class AvailabilityResult {
        private final boolean available;
        private final String message;
        private final RentalBooking conflictingBooking;

        public AvailabilityResult(boolean available, String message, RentalBooking conflictingBooking) {
            this.available = available;
            this.message = message;
            this.conflictingBooking = conflictingBooking;
        }

        public boolean isAvailable() { return available; }
        public String getMessage() { return message; }
        public RentalBooking getConflictingBooking() { return conflictingBooking; }
    }
}

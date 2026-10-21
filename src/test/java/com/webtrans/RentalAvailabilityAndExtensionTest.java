package com.webtrans;

import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import com.webtrans.vehicle_rental.controller.RentalController;
import com.webtrans.vehicle_rental.dto.RentalExtensionQuoteDto;
import com.webtrans.vehicle_rental.dto.RentalQuoteDto;
import com.webtrans.vehicle_rental.model.RentalBooking;
import com.webtrans.vehicle_rental.model.RentalVehicle;
import com.webtrans.vehicle_rental.repository.RentalBookingRepository;
import com.webtrans.vehicle_rental.repository.RentalVehicleRepository;
import com.webtrans.vehicle_rental.service.RentalAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RentalAvailabilityAndExtensionTest {

    @Autowired
    private RentalAvailabilityService availabilityService;

    @Autowired
    private RentalController rentalController;

    @Autowired
    private RentalVehicleRepository vehicleRepository;

    @Autowired
    private RentalBookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private User testCustomerA;
    private User testCustomerB;
    private RentalVehicle testVehicle;

    @BeforeEach
    void setUp() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.execute(status -> {
            testCustomerA = userRepository.findByEmail("rental_customer_a@test.com").orElseGet(() -> {
                User u = new User();
                u.setEmail("rental_customer_a@test.com");
                u.setFullName("Customer Alice");
                u.setPassword("testpass123");
                return userRepository.save(u);
            });

            testCustomerB = userRepository.findByEmail("rental_customer_b@test.com").orElseGet(() -> {
                User u = new User();
                u.setEmail("rental_customer_b@test.com");
                u.setFullName("Customer Bob");
                u.setPassword("testpass123");
                return userRepository.save(u);
            });

            // Find or create test vehicle
            List<RentalVehicle> existing = vehicleRepository.findAll();
            testVehicle = existing.stream()
                    .filter(v -> "Test Aqua Hybrid".equals(v.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        RentalVehicle v = new RentalVehicle();
                        v.setName("Test Aqua Hybrid");
                        v.setType("Car");
                        v.setDailyRate(5500.0);
                        v.setStatus("AVAILABLE");
                        v.setImageUrl("https://example.com/test-aqua.jpg");
                        return vehicleRepository.save(v);
                    });

            // Clean up any existing bookings for the test vehicle so tests start clean
            List<RentalBooking> bookings = bookingRepository.findByVehicle_IdAndStatusIn(
                    testVehicle.getId(),
                    List.of("PENDING", "CONFIRMED", "COMPLETED", "CANCELLED")
            );
            bookingRepository.deleteAll(bookings);

            return null;
        });
    }

    @Test
    @DisplayName("1. Day-Count & Pricing: 24-hr day periods, same-day min 1 day, BigDecimal calculations")
    void testBillableDaysAndDayCountRule() {
        LocalDate today = LocalDate.now();
        LocalDate future1 = today.plusDays(30);

        // Same day -> 1 billable day
        RentalQuoteDto sameDayQuote = availabilityService.calculateQuote(testVehicle.getId(), future1, future1);
        assertEquals(1, sameDayQuote.getBillableDays());
        assertEquals(new BigDecimal("5500.00"), sameDayQuote.getSubtotal());
        assertEquals(new BigDecimal("5500.00"), sameDayQuote.getTotal());
        assertTrue(sameDayQuote.isAvailable());

        // 3 days range: e.g. Day 30 to Day 33 -> 3 days
        LocalDate future2 = future1.plusDays(3);
        RentalQuoteDto multiDayQuote = availabilityService.calculateQuote(testVehicle.getId(), future1, future2);
        assertEquals(3, multiDayQuote.getBillableDays());
        assertEquals(new BigDecimal("16500.00"), multiDayQuote.getSubtotal());
        assertEquals(new BigDecimal("16500.00"), multiDayQuote.getTotal());

        // Start date in past -> unavailable quote
        RentalQuoteDto pastQuote = availabilityService.calculateQuote(testVehicle.getId(), today.minusDays(1), today.plusDays(1));
        assertFalse(pastQuote.isAvailable());
        assertTrue(pastQuote.getAvailabilityMessage().contains("past"));

        // End date before start date -> unavailable quote
        RentalQuoteDto invertedQuote = availabilityService.calculateQuote(testVehicle.getId(), future2, future1);
        assertFalse(invertedQuote.isAvailable());
    }

    @Test
    @DisplayName("2. Future availability: Vehicle rented today is available for a future non-overlapping period")
    void testVehicleRentedTodayBookableInFuture() {
        LocalDate today = LocalDate.now();
        LocalDate returnToday = today.plusDays(2); // rented today until 2 days later

        // Create booking for today to +2 days
        availabilityService.createBookingAtomic(
                testVehicle.getId(), testCustomerA, testCustomerA.getFullName(), "0771234567", today, returnToday);

        // Check availability for tomorrow (during current booking) -> MUST BE UNAVAILABLE
        var midRentalAvail = availabilityService.checkVehicleAvailability(
                testVehicle.getId(), today.plusDays(1), today.plusDays(3), null);
        assertFalse(midRentalAvail.isAvailable(), "Vehicle must be unavailable during active rental period");

        // Check availability for NEXT MONTH -> MUST BE AVAILABLE!
        LocalDate nextMonthStart = today.plusDays(20);
        LocalDate nextMonthEnd = today.plusDays(25);
        var futureAvail = availabilityService.checkVehicleAvailability(
                testVehicle.getId(), nextMonthStart, nextMonthEnd, null);
        assertTrue(futureAvail.isAvailable(), "Vehicle rented today MUST be bookable for future unreserved dates");
    }

    @Test
    @DisplayName("3. Interval Boundaries: A booking ending on date D allows a new booking to start on date D")
    void testIntervalBoundaries() {
        LocalDate startA = LocalDate.now().plusDays(40);
        LocalDate endA = startA.plusDays(5); // [Day 40, Day 45)

        availabilityService.createBookingAtomic(
                testVehicle.getId(), testCustomerA, testCustomerA.getFullName(), "0771112233", startA, endA);

        // Booking B starting exactly when Booking A ends (endA)
        LocalDate startB = endA; // Day 45
        LocalDate endB = startB.plusDays(3); // Day 48
        var availableAtBoundary = availabilityService.checkVehicleAvailability(testVehicle.getId(), startB, endB, null);
        assertTrue(availableAtBoundary.isAvailable(), "New booking starting on previous return date must be permitted (drop-off boundary)");

        // Booking C ending on date startA
        LocalDate startC = startA.minusDays(3);
        LocalDate endC = startA;
        var availableBeforeBoundary = availabilityService.checkVehicleAvailability(testVehicle.getId(), startC, endC, null);
        assertTrue(availableBeforeBoundary.isAvailable(), "Booking ending when next booking starts must be permitted");

        // Booking D overlapping by 1 day
        LocalDate startD = endA.minusDays(1);
        LocalDate endD = endA.plusDays(2);
        var availableOverlap = availabilityService.checkVehicleAvailability(testVehicle.getId(), startD, endD, null);
        assertFalse(availableOverlap.isAvailable(), "Overlapping by even 1 day must be rejected");
    }

    @Test
    @DisplayName("4. Cancelled bookings immediately release reserved dates")
    void testCancelledBookingReleasesDates() throws Exception {
        LocalDate start = LocalDate.now().plusDays(60);
        LocalDate end = start.plusDays(4);

        RentalBooking saved = availabilityService.createBookingAtomic(
                testVehicle.getId(), testCustomerA, testCustomerA.getFullName(), "0779998888", start, end);

        // Dates are blocked
        assertFalse(availabilityService.checkVehicleAvailability(testVehicle.getId(), start, end, null).isAvailable());

        // Cancel booking as the owner
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", testCustomerA);
        MockHttpServletResponse response = new MockHttpServletResponse();
        rentalController.updateBookingStatus(saved.getId(), "CANCELLED", "index", session, response, new RedirectAttributesModelMap());

        // Dates must now be available
        assertTrue(availabilityService.checkVehicleAvailability(testVehicle.getId(), start, end, null).isAvailable(),
                "Cancelled booking dates must be immediately released for other customers");
    }

    @Test
    @DisplayName("5. Server rejects price tampering and records authoritative calculation")
    void testPriceTamperingProtection() {
        LocalDate start = LocalDate.now().plusDays(70);
        LocalDate end = start.plusDays(2); // 2 days @ 5500 = 11000

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", testCustomerA);

        RentalBooking booking = new RentalBooking();
        booking.setStartDate(start);
        booking.setEndDate(end);

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        // Client attempts to book; the controller calls createBookingAtomic which calculates price authoritatively on the server
        String result = rentalController.bookVehicle(
                testVehicle.getId(),
                "Customer Alice",
                "0770001122",
                start,
                end,
                session,
                redirectAttributes
        );

        assertEquals("redirect:/rental/index", result);

        // Find the saved booking and verify it used the genuine vehicle daily rate (5500.0) and cost (11000.0)
        List<RentalBooking> bookings = bookingRepository.findByCustomerUser_IdOrderByIdDesc(testCustomerA.getId());
        assertFalse(bookings.isEmpty());
        RentalBooking saved = bookings.get(0);
        assertEquals(5500.0, saved.getDailyRate(), 0.01);
        assertEquals(11000.0, saved.getTotalCost(), 0.01);
        assertEquals(2, saved.getDays());
        assertEquals(testCustomerA.getId(), saved.getCustomerUser().getId());
    }

    @Test
    @DisplayName("6. Successful rental extension updates booking, billable days, and records audit trail")
    void testSuccessfulRentalExtension() {
        LocalDate start = LocalDate.now().plusDays(80);
        LocalDate end = start.plusDays(3); // 3 days

        RentalBooking saved = availabilityService.createBookingAtomic(
                testVehicle.getId(), testCustomerA, testCustomerA.getFullName(), "0773334444", start, end);

        assertEquals(3, saved.getDays());
        assertEquals(16500.0, saved.getTotalCost(), 0.01);

        // Extend by 2 days: from Day 83 to Day 85
        LocalDate newEnd = end.plusDays(2);

        // Preview quote
        RentalExtensionQuoteDto preview = availabilityService.previewExtension(saved.getId(), testCustomerA, newEnd);
        assertTrue(preview.isAvailable());
        assertEquals(2, preview.getAdditionalDays());
        assertEquals(new BigDecimal("11000.00"), preview.getAdditionalCost());
        assertEquals(new BigDecimal("27500.00"), preview.getNewTotalCost());

        // Perform extension
        RentalBooking extended = availabilityService.extendBookingAtomic(saved.getId(), testCustomerA, newEnd);

        assertEquals(newEnd, extended.getEndDate());
        assertEquals(5, extended.getDays());
        assertEquals(27500.0, extended.getTotalCost(), 0.01);
        assertEquals(end, extended.getExtendedFromDate());
        assertEquals(11000.0, extended.getExtensionCost(), 0.01);
        assertNotNull(extended.getExtendedAt());
    }

    @Test
    @DisplayName("7. Extension conflict: Extension is rejected when it overlaps another customer's booking")
    void testExtensionConflictRejected() {
        LocalDate start1 = LocalDate.now().plusDays(90);
        LocalDate end1 = start1.plusDays(3); // Oct 90 to Oct 93 (Alice)

        RentalBooking saved1 = availabilityService.createBookingAtomic(
                testVehicle.getId(), testCustomerA, testCustomerA.getFullName(), "0771231234", start1, end1);

        LocalDate start2 = end1.plusDays(2); // Oct 95 to Oct 98 (Bob)
        LocalDate end2 = start2.plusDays(3);

        availabilityService.createBookingAtomic(
                testVehicle.getId(), testCustomerB, testCustomerB.getFullName(), "0775556666", start2, end2);

        // Alice tries to extend booking1 to Oct 96 (overlaps Bob's booking starting Oct 95)
        LocalDate conflictingEnd = start2.plusDays(1);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                availabilityService.extendBookingAtomic(saved1.getId(), testCustomerA, conflictingEnd)
        );
        assertTrue(ex.getMessage().contains("already reserved"), "Should reject extension due to conflict with Bob's booking");

        // Verify booking1 is completely unchanged
        RentalBooking unchanged = bookingRepository.findById(saved1.getId()).orElseThrow();
        assertEquals(end1, unchanged.getEndDate());
        assertEquals(3, unchanged.getDays());
        assertEquals(16500.0, unchanged.getTotalCost(), 0.01);
        assertNull(unchanged.getExtendedFromDate());
    }

    @Test
    @DisplayName("8. Ownership Security: Customer cannot extend or cancel another customer's booking")
    void testOwnershipSecurityEnforced() throws Exception {
        LocalDate start = LocalDate.now().plusDays(110);
        LocalDate end = start.plusDays(2);

        RentalBooking saved = availabilityService.createBookingAtomic(
                testVehicle.getId(), testCustomerA, testCustomerA.getFullName(), "0778881122", start, end);

        // Customer Bob tries to extend Alice's booking
        assertThrows(SecurityException.class, () ->
                availabilityService.extendBookingAtomic(saved.getId(), testCustomerB, end.plusDays(1))
        );

        // Customer Bob tries to cancel Alice's booking via controller
        MockHttpSession bobSession = new MockHttpSession();
        bobSession.setAttribute("loggedUser", testCustomerB);
        MockHttpServletResponse bobResponse = new MockHttpServletResponse();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        rentalController.updateBookingStatus(saved.getId(), "CANCELLED", "index", bobSession, bobResponse, redirectAttributes);
        assertEquals(403, bobResponse.getStatus(), "Must return 403 Forbidden when unauthorized user tries to cancel");

        // Verify booking is still PENDING and owned by Alice
        RentalBooking reloaded = bookingRepository.findById(saved.getId()).orElseThrow();
        assertEquals("PENDING", reloaded.getStatus());
        assertEquals(testCustomerA.getId(), reloaded.getCustomerUser().getId());
    }

    @Test
    @DisplayName("9. Image URLs and local SVG fallbacks are safe and context-path aware")
    void testVehicleImageFallbackResolutions() {
        RentalVehicle googleUrlVehicle = new RentalVehicle();
        googleUrlVehicle.setType("Car");
        googleUrlVehicle.setImageUrl("https://www.google.com/imgres?q=aqua&imgurl=https://example.com/pic.jpg");

        // Must reject google search link and resolve to clean local vector SVG
        String resolvedGoogle = googleUrlVehicle.resolveImageUrl("");
        assertEquals("/rental/images/car.svg", resolvedGoogle);

        RentalVehicle emptyUrlVehicle = new RentalVehicle();
        emptyUrlVehicle.setType("Van");
        emptyUrlVehicle.setImageUrl("");
        String resolvedEmpty = emptyUrlVehicle.resolveImageUrl("/my-app");
        assertEquals("/my-app/rental/images/van.svg", resolvedEmpty);

        RentalVehicle validUrlVehicle = new RentalVehicle();
        validUrlVehicle.setType("Luxury");
        validUrlVehicle.setImageUrl("https://cdn.example.com/clean-bmw.png");
        String resolvedValid = validUrlVehicle.resolveImageUrl("");
        assertEquals("https://cdn.example.com/clean-bmw.png", resolvedValid);
    }

    @Test
    @DisplayName("10. Concurrency: Two concurrent booking attempts for the same vehicle and period")
    void testConcurrentBookingAttemptsRowLocking() throws Exception {
        LocalDate start = LocalDate.now().plusDays(130);
        LocalDate end = start.plusDays(3);

        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger conflicts = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // ensure both threads strike simultaneously
                    availabilityService.createBookingAtomic(
                            testVehicle.getId(),
                            idx == 0 ? testCustomerA : testCustomerB,
                            "Concurrent User " + idx,
                            "077000000" + idx,
                            start,
                            end
                    );
                    successes.incrementAndGet();
                } catch (IllegalStateException e) {
                    conflicts.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Release both threads
        finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Exactly ONE must succeed and the other must be rejected with conflict
        assertEquals(1, successes.get(), "Exactly one concurrent booking attempt must succeed");
        assertEquals(1, conflicts.get(), "The competing concurrent booking attempt must be cleanly rejected");
    }
}

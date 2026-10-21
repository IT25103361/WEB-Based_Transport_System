package com.webtrans;

import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import com.webtrans.ridebook.controller.BookingController;
import com.webtrans.ridebook.controller.TrackingController;
import com.webtrans.ridebook.model.Booking;
import com.webtrans.ridebook.model.Driver;
import com.webtrans.ridebook.repository.BookingRepository;
import com.webtrans.ridebook.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PassengerBookingOwnershipTest {

    @Autowired
    private BookingController bookingController;

    @Autowired
    private TrackingController trackingController;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    private User userDinuja;
    private User userChathuranga;
    private User userDinujaTwo; // Same display name "dinuja", different ID

    @BeforeEach
    void setUp() {
        // Ensure user 1 (Dinuja) exists
        userDinuja = userRepository.findByEmail("bandaradinuja65@gmail.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("bandaradinuja65@gmail.com");
            u.setFullName("dinuja");
            u.setPassword("1234");
            return userRepository.save(u);
        });

        // Ensure user 2 (Chathuranga) exists
        userChathuranga = userRepository.findByEmail("couple1@mail.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("couple1@mail.com");
            u.setFullName("chathuranga");
            u.setPassword("1234");
            return userRepository.save(u);
        });

        // Ensure a user with same display name "dinuja" exists with different email & ID
        userDinujaTwo = userRepository.findByEmail("dinuja.duplicate@mail.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("dinuja.duplicate@mail.com");
            u.setFullName("dinuja");
            u.setPassword("1234");
            return userRepository.save(u);
        });
    }

    @Test
    @DisplayName("1. Persist booking ownership strictly from session, ignoring spoofed names")
    void testBookingOwnershipPersistence() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", userDinuja);
        MockHttpServletResponse response = new MockHttpServletResponse();

        Booking newBooking = new Booking();
        newBooking.setPickupLocation("Colombo Fort");
        newBooking.setDropLocation("Kandy");
        newBooking.setVehicleType("Car");
        newBooking.setFare(3500.0);
        newBooking.setPassengerName("SpoofedDisplay"); // Even if submitted name is different

        String result = bookingController.createBooking(newBooking, session, response);
        assertEquals("redirect:/ride/passenger", result);
        assertNotNull(newBooking.getId());
        assertNotNull(newBooking.getPassengerUser());
        assertEquals(userDinuja.getId(), newBooking.getPassengerUser().getId());
        assertEquals(userDinuja.getId(), newBooking.getPassengerId());
    }

    @Test
    @DisplayName("2. Reject unauthenticated booking creation with 401")
    void testRejectUnauthenticatedBooking() throws Exception {
        MockHttpSession session = new MockHttpSession(); // No loggedUser
        MockHttpServletResponse response = new MockHttpServletResponse();

        Booking newBooking = new Booking();
        newBooking.setPickupLocation("Galle");
        newBooking.setDropLocation("Matara");
        newBooking.setFare(1500.0);

        String result = bookingController.createBooking(newBooking, session, response);
        assertNull(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("3. Server-side filtering: Dinuja and Chathuranga see strictly their own bookings")
    void testServerSideFilteringPerUser() throws Exception {
        // Create booking for Dinuja
        Booking bDinuja = new Booking();
        bDinuja.setPassengerUser(userDinuja);
        bDinuja.setPassengerName("dinuja");
        bDinuja.setPickupLocation("Nugegoda");
        bDinuja.setDropLocation("Maharagama");
        bDinuja.setStatus("PENDING");
        bDinuja.setFare(500.0);
        bookingRepository.save(bDinuja);

        // Create booking for Chathuranga
        Booking bChathuranga = new Booking();
        bChathuranga.setPassengerUser(userChathuranga);
        bChathuranga.setPassengerName("chathuranga");
        bChathuranga.setPickupLocation("Gampaha");
        bChathuranga.setDropLocation("Yakkala");
        bChathuranga.setStatus("PENDING");
        bChathuranga.setFare(400.0);
        bookingRepository.save(bChathuranga);

        // Fetch Dinuja's passenger view
        MockHttpSession dinujaSession = new MockHttpSession();
        dinujaSession.setAttribute("loggedUser", userDinuja);
        Model dinujaModel = new ConcurrentModel();
        MockHttpServletResponse dinujaResponse = new MockHttpServletResponse();

        bookingController.passengerView(dinujaModel, dinujaSession, dinujaResponse);
        @SuppressWarnings("unchecked")
        List<Booking> dinujaBookings = (List<Booking>) dinujaModel.getAttribute("bookings");
        assertNotNull(dinujaBookings);
        assertTrue(dinujaBookings.stream().anyMatch(b -> b.getId().equals(bDinuja.getId())));
        assertFalse(dinujaBookings.stream().anyMatch(b -> b.getId().equals(bChathuranga.getId())));

        // Fetch Chathuranga's passenger view
        MockHttpSession chathurangaSession = new MockHttpSession();
        chathurangaSession.setAttribute("loggedUser", userChathuranga);
        Model chathurangaModel = new ConcurrentModel();
        MockHttpServletResponse chathurangaResponse = new MockHttpServletResponse();

        bookingController.passengerView(chathurangaModel, chathurangaSession, chathurangaResponse);
        @SuppressWarnings("unchecked")
        List<Booking> chathurangaBookings = (List<Booking>) chathurangaModel.getAttribute("bookings");
        assertNotNull(chathurangaBookings);
        assertTrue(chathurangaBookings.stream().anyMatch(b -> b.getId().equals(bChathuranga.getId())));
        assertFalse(chathurangaBookings.stream().anyMatch(b -> b.getId().equals(bDinuja.getId())));
    }

    @Test
    @DisplayName("4. Two users with same display name have completely isolated histories")
    void testSameDisplayNameIsolation() throws Exception {
        Booking b1 = new Booking();
        b1.setPassengerUser(userDinuja);
        b1.setPassengerName("dinuja");
        b1.setStatus("PENDING");
        bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setPassengerUser(userDinujaTwo); // Also display name "dinuja", but different user ID!
        b2.setPassengerName("dinuja");
        b2.setStatus("PENDING");
        bookingRepository.save(b2);

        // Dinuja 1 view
        MockHttpSession session1 = new MockHttpSession();
        session1.setAttribute("loggedUser", userDinuja);
        Model model1 = new ConcurrentModel();
        bookingController.passengerView(model1, session1, new MockHttpServletResponse());
        @SuppressWarnings("unchecked")
        List<Booking> list1 = (List<Booking>) model1.getAttribute("bookings");
        assertTrue(list1.stream().anyMatch(b -> b.getId().equals(b1.getId())));
        assertFalse(list1.stream().anyMatch(b -> b.getId().equals(b2.getId())));

        // Dinuja 2 view
        MockHttpSession session2 = new MockHttpSession();
        session2.setAttribute("loggedUser", userDinujaTwo);
        Model model2 = new ConcurrentModel();
        bookingController.passengerView(model2, session2, new MockHttpServletResponse());
        @SuppressWarnings("unchecked")
        List<Booking> list2 = (List<Booking>) model2.getAttribute("bookings");
        assertTrue(list2.stream().anyMatch(b -> b.getId().equals(b2.getId())));
        assertFalse(list2.stream().anyMatch(b -> b.getId().equals(b1.getId())));
    }

    @Test
    @DisplayName("5. Legacy unowned bookings (passenger_id = null) do not appear in any user history")
    void testLegacyUnownedBookingsHidden() throws Exception {
        Booking legacyBooking = new Booking();
        legacyBooking.setPassengerUser(null);
        legacyBooking.setPassengerName("dinuja"); // Old booking created without passenger_id
        legacyBooking.setStatus("COMPLETED");
        legacyBooking.setFare(200.0);
        bookingRepository.save(legacyBooking);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", userDinuja);
        Model model = new ConcurrentModel();
        bookingController.passengerView(model, session, new MockHttpServletResponse());

        @SuppressWarnings("unchecked")
        List<Booking> list = (List<Booking>) model.getAttribute("bookings");
        assertNotNull(list);
        assertFalse(list.stream().anyMatch(b -> b.getId().equals(legacyBooking.getId())),
                "Unowned legacy booking must not appear in ordinary passenger history until explicitly linked.");
    }

    @Test
    @DisplayName("6. Protect cancel: 403 when User 2 attempts to cancel User 1's booking")
    void testProtectCancelAction() throws Exception {
        Booking b = new Booking();
        b.setPassengerUser(userDinuja);
        b.setPassengerName("dinuja");
        b.setStatus("PENDING");
        bookingRepository.save(b);

        MockHttpSession sessionUser2 = new MockHttpSession();
        sessionUser2.setAttribute("loggedUser", userChathuranga);
        MockHttpServletResponse response = new MockHttpServletResponse();

        String result = bookingController.cancelBooking(b.getId(), sessionUser2, response);
        assertNull(result);
        assertEquals(403, response.getStatus());

        // Booking status must still be PENDING (unmodified)
        Booking refreshed = bookingRepository.findById(b.getId()).orElseThrow();
        assertEquals("PENDING", refreshed.getStatus());
    }

    @Test
    @DisplayName("7. Protect payment: 403 when User 2 attempts to view payment or pay for User 1's booking")
    void testProtectPaymentAction() throws Exception {
        Booking b = new Booking();
        b.setPassengerUser(userDinuja);
        b.setPassengerName("dinuja");
        b.setStatus("COMPLETED");
        b.setFare(1200.0);
        bookingRepository.save(b);

        MockHttpSession sessionUser2 = new MockHttpSession();
        sessionUser2.setAttribute("loggedUser", userChathuranga);

        // Test payment page access
        MockHttpServletResponse pageResponse = new MockHttpServletResponse();
        String pageResult = bookingController.paymentPage(b.getId(), new ConcurrentModel(), sessionUser2, pageResponse);
        assertNull(pageResult);
        assertEquals(403, pageResponse.getStatus());

        // Test payment processing
        MockHttpServletResponse payResponse = new MockHttpServletResponse();
        String payResult = bookingController.processPayment(b.getId(), "WELCOME10", 1080.0, sessionUser2, payResponse);
        assertNull(payResult);
        assertEquals(403, payResponse.getStatus());

        // Booking status must still be COMPLETED (not PAID)
        Booking refreshed = bookingRepository.findById(b.getId()).orElseThrow();
        assertEquals("COMPLETED", refreshed.getStatus());
    }

    @Test
    @DisplayName("8. Protect tracking API: 403 when unauthorized user attempts to track another user's ride")
    void testProtectTrackingApi() {
        Booking b = new Booking();
        b.setPassengerUser(userDinuja);
        b.setPassengerName("dinuja");
        b.setStatus("ACCEPTED");
        b.setPickupLat(6.9271);
        b.setPickupLng(79.8612);
        bookingRepository.save(b);

        MockHttpSession sessionUser2 = new MockHttpSession();
        sessionUser2.setAttribute("loggedUser", userChathuranga);

        ResponseEntity<?> response = trackingController.getTrackingInfo(b.getId(), sessionUser2);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("9. Assigned driver CAN still track active ride")
    void testDriverCanTrackAssignedRide() {
        // Create driver profile linked to userChathuranga
        List<Driver> existingDrivers = driverRepository.findAllByUserId(userChathuranga.getId());
        Driver driver = existingDrivers.isEmpty() ? null : existingDrivers.get(0);
        if (driver == null) {
            driver = new Driver();
            driver.setUserId(userChathuranga.getId());
            driver.setDriverName("Driver Chathuranga");
            driver.setVehicleType("Car");
            driver.setVehicleNumber("WP-CA-1234");
            driver.setStatus("BUSY");
            driver = driverRepository.save(driver);
        }

        Booking b = new Booking();
        b.setPassengerUser(userDinuja);
        b.setPassengerName("dinuja");
        b.setStatus("ACCEPTED");
        b.setDriver(driver);
        b.setPickupLat(6.9271);
        b.setPickupLng(79.8612);
        bookingRepository.save(b);

        MockHttpSession driverSession = new MockHttpSession();
        driverSession.setAttribute("loggedUser", userChathuranga);

        ResponseEntity<?> response = trackingController.getTrackingInfo(b.getId(), driverSession);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

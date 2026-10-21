package com.webtrans;

import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import com.webtrans.auth.service.PasswordService;
import com.webtrans.ridebook.controller.BookingController;
import com.webtrans.ridebook.controller.RideAuthController;
import com.webtrans.ridebook.model.Booking;
import com.webtrans.ridebook.model.Driver;
import com.webtrans.ridebook.repository.BookingRepository;
import com.webtrans.ridebook.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DriverProximityAndLogoutTest {

    @Autowired
    private RideAuthController rideAuthController;

    @Autowired
    private BookingController bookingController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordService passwordService;

    private User mainUser;
    private User driverAccountUser;
    private Driver driver;

    @BeforeEach
    void setup() {
        // Base account user (e.g. Passenger/Home session)
        mainUser = new User();
        mainUser.setEmail("main_passenger_" + System.currentTimeMillis() + "@test.com");
        mainUser.setFullName("Base Account User");
        mainUser.setPassword(passwordService.encode("Pass123!"));
        mainUser = userRepository.save(mainUser);

        // Driver account user
        driverAccountUser = new User();
        driverAccountUser.setEmail("driver_account_" + System.currentTimeMillis() + "@test.com");
        driverAccountUser.setFullName("Test Driver Amal");
        driverAccountUser.setPassword(passwordService.encode("DriverPass123!"));
        driverAccountUser = userRepository.save(driverAccountUser);

        // Driver profile located in Colombo (lat=6.9281, lng=79.8622)
        driver = new Driver();
        driver.setUserId(driverAccountUser.getId());
        driver.setDriverName("Test Driver Amal");
        driver.setPhoneNumber("0771234567");
        driver.setVehicleNumber("WP-CAR-7777");
        driver.setVehicleType("Car");
        driver.setStatus("AVAILABLE");
        driver.setCurrentLat(6.9281);
        driver.setCurrentLng(79.8622);
        driver = driverRepository.save(driver);
    }

    @Test
    @DisplayName("Requirement 1: Isolated Driver Logout ends only driver session and preserves global base user session")
    void testDriverLogoutPreservesGlobalSession() {
        MockHttpSession session = new MockHttpSession();
        // Both global user session and driver role session are active
        session.setAttribute("loggedUser", mainUser);
        session.setAttribute("userRole", "PASSENGER");
        session.setAttribute("driverUser", driverAccountUser);
        session.setAttribute("loggedDriver", driver);
        session.setAttribute("rideSelectedDriverId", driver.getId());

        // Perform Driver Logout
        String redirect = rideAuthController.driverLogout(session);

        // Safe redirect back to role selection hub
        assertEquals("redirect:/ride/ride-hub", redirect);

        // Active driver role state is cleared
        assertNull(session.getAttribute("driverUser"));
        assertNull(session.getAttribute("loggedDriver"));
        assertNull(session.getAttribute("rideSelectedDriverId"));

        // Global base account session is preserved and session is NOT invalidated
        assertNotNull(session.getAttribute("loggedUser"));
        assertEquals(mainUser.getId(), ((User) session.getAttribute("loggedUser")).getId());
        assertEquals("PASSENGER", session.getAttribute("userRole"));
        assertFalse(session.isInvalid());
    }

    @Test
    @DisplayName("Requirement 2: Available Ride Requests only displays rides within 5 KM radius of driver's live location")
    void testProximityFilterOnlyDisplaysRidesWithin5Km() {
        // Nearby booking (~2.3 KM from driver in Colombo)
        Booking nearbyRide = new Booking();
        nearbyRide.setPassengerUser(mainUser);
        nearbyRide.setPassengerName("Nearby Passenger");
        nearbyRide.setPickupLocation("Colombo Fort");
        nearbyRide.setDropLocation("Kollupitiya");
        nearbyRide.setPickupLat(6.9344);
        nearbyRide.setPickupLng(79.8428);
        nearbyRide.setVehicleType("Car");
        nearbyRide.setStatus("PENDING");
        nearbyRide = bookingRepository.save(nearbyRide);

        // Far booking (~83 KM away in Kurunegala)
        Booking farRide = new Booking();
        farRide.setPassengerUser(mainUser);
        farRide.setPassengerName("Far Passenger");
        farRide.setPickupLocation("Kurunegala Town");
        farRide.setDropLocation("Pothuhera");
        farRide.setPickupLat(7.4870);
        farRide.setPickupLng(80.3649);
        farRide.setVehicleType("Car");
        farRide.setStatus("PENDING");
        farRide = bookingRepository.save(farRide);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("driverUser", driverAccountUser);
        session.setAttribute("loggedDriver", driver);
        Model model = new ConcurrentModel();
        MockHttpServletResponse response = new MockHttpServletResponse();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = bookingController.driverView(model, session, response, redirectAttributes);
        assertEquals("ride/driver", view);

        @SuppressWarnings("unchecked")
        List<Booking> pendingBookings = (List<Booking>) model.getAttribute("pendingBookings");
        assertNotNull(pendingBookings);

        final Long nearbyId = nearbyRide.getId();
        final Long farId = farRide.getId();

        // Nearby ride (2.3 KM <= 5 KM) MUST be present
        assertTrue(pendingBookings.stream().anyMatch(b -> b.getId().equals(nearbyId)),
                "Nearby ride within 5 KM should appear under Available Ride Requests");

        // Far ride (83 KM > 5 KM) MUST NOT be present
        assertFalse(pendingBookings.stream().anyMatch(b -> b.getId().equals(farId)),
                "Far ride beyond 5 KM should NOT appear under Available Ride Requests");
    }

    @Test
    @DisplayName("Requirement 2: Driver cannot accept a ride located farther than 5 KM")
    void testAcceptRideRejectsFartherThan5Km() {
        Booking farRide = new Booking();
        farRide.setPassengerUser(mainUser);
        farRide.setPassengerName("Far Passenger");
        farRide.setPickupLocation("Kurunegala Town");
        farRide.setDropLocation("Pothuhera");
        farRide.setPickupLat(7.4870);
        farRide.setPickupLng(80.3649);
        farRide.setVehicleType("Car");
        farRide.setStatus("PENDING");
        farRide = bookingRepository.save(farRide);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("driverUser", driverAccountUser);
        session.setAttribute("loggedDriver", driver);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = bookingController.acceptRide(farRide.getId(), session, redirectAttributes);
        assertEquals("redirect:/ride/driver", view);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("errorMsg"));
        assertTrue(redirectAttributes.getFlashAttributes().get("errorMsg").toString().contains("5 KM"));

        // Booking remains unassigned and PENDING
        Booking reloaded = bookingRepository.findById(farRide.getId()).orElseThrow();
        assertEquals("PENDING", reloaded.getStatus());
        assertNull(reloaded.getDriver());
    }

    @Test
    @DisplayName("Requirement 2: Driver can accept a ride located within 5 KM")
    void testAcceptRideAllowsWithin5Km() {
        Booking nearbyRide = new Booking();
        nearbyRide.setPassengerUser(mainUser);
        nearbyRide.setPassengerName("Nearby Passenger");
        nearbyRide.setPickupLocation("Colombo Fort");
        nearbyRide.setDropLocation("Kollupitiya");
        nearbyRide.setPickupLat(6.9344);
        nearbyRide.setPickupLng(79.8428);
        nearbyRide.setVehicleType("Car");
        nearbyRide.setStatus("PENDING");
        nearbyRide = bookingRepository.save(nearbyRide);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("driverUser", driverAccountUser);
        session.setAttribute("loggedDriver", driver);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = bookingController.acceptRide(nearbyRide.getId(), session, redirectAttributes);
        assertEquals("redirect:/ride/driver", view);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("successMsg"));

        // Booking is accepted and assigned to driver
        Booking reloaded = bookingRepository.findById(nearbyRide.getId()).orElseThrow();
        assertEquals("ACCEPTED", reloaded.getStatus());
        assertNotNull(reloaded.getDriver());
        assertEquals(driver.getId(), reloaded.getDriver().getId());
    }
}

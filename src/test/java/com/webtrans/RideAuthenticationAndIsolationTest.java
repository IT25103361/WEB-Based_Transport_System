package com.webtrans;

import com.webtrans.auth.config.AuthInterceptor;
import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import com.webtrans.auth.service.PasswordService;
import com.webtrans.ridebook.controller.BookingController;
import com.webtrans.ridebook.controller.RideAuthController;
import com.webtrans.ridebook.controller.TrackingController;
import com.webtrans.ridebook.model.Booking;
import com.webtrans.ridebook.model.Driver;
import com.webtrans.ridebook.repository.BookingRepository;
import com.webtrans.ridebook.repository.DriverRepository;
import com.webtrans.ridebook.service.DriverProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
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
public class RideAuthenticationAndIsolationTest {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private RideAuthController rideAuthController;

    @Autowired
    private BookingController bookingController;

    @Autowired
    private TrackingController trackingController;

    @Autowired
    private DriverProfileService driverProfileService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User passengerUser;
    private User driverUser;
    private Driver driverProfile;

    @BeforeEach
    void setupData() {
        // Unique test users
        String pEmail = "test_passenger_" + System.currentTimeMillis() + "@test.com";
        passengerUser = new User();
        passengerUser.setEmail(pEmail);
        passengerUser.setFullName("Test Passenger");
        passengerUser.setPassword("plainPass123"); // legacy plaintext to test compatibility & upgrade
        passengerUser = userRepository.save(passengerUser);

        String dEmail = "test_driver_" + System.currentTimeMillis() + "@test.com";
        driverUser = new User();
        driverUser.setEmail(dEmail);
        driverUser.setFullName("Test Driver Sunil");
        driverUser.setPassword(passwordService.encode("driverPass123"));
        driverUser = userRepository.save(driverUser);

        driverProfile = new Driver();
        driverProfile.setUserId(driverUser.getId());
        driverProfile.setDriverName("Test Driver Sunil");
        driverProfile.setPhoneNumber("0779998888");
        driverProfile.setVehicleNumber("WP-TST-1111");
        driverProfile.setVehicleType("Car");
        driverProfile.setStatus("AVAILABLE");
        driverProfile = driverRepository.save(driverProfile);
    }

    @Test
    @DisplayName("1. Public endpoints: Portal Selection and Auth pages accessible without login")
    void testPublicEndpointsAccessibility() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Hub and Portal selection
        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/ride/ride-hub");
        assertTrue(authInterceptor.preHandle(req1, response, new Object()));

        // Passenger auth routes
        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/ride/passenger/login");
        assertTrue(authInterceptor.preHandle(req2, response, new Object()));

        MockHttpServletRequest req3 = new MockHttpServletRequest("POST", "/ride/passenger/register");
        assertTrue(authInterceptor.preHandle(req3, response, new Object()));

        // Driver auth routes
        MockHttpServletRequest req4 = new MockHttpServletRequest("GET", "/ride/driver/login");
        assertTrue(authInterceptor.preHandle(req4, response, new Object()));

        MockHttpServletRequest req5 = new MockHttpServletRequest("POST", "/ride/driver/register");
        assertTrue(authInterceptor.preHandle(req5, response, new Object()));

        // Logout
        MockHttpServletRequest req6 = new MockHttpServletRequest("GET", "/ride/logout");
        assertTrue(authInterceptor.preHandle(req6, response, new Object()));
    }

    @Test
    @DisplayName("2. Interceptor redirects unauthenticated passenger and driver routes to their respective logins")
    void testInterceptorRoleSpecificRedirects() throws Exception {
        // Unauthenticated passenger route redirects to /ride/passenger/login
        MockHttpServletRequest passReq = new MockHttpServletRequest("GET", "/ride/passenger");
        MockHttpServletResponse passRes = new MockHttpServletResponse();
        boolean passAllowed = authInterceptor.preHandle(passReq, passRes, new Object());
        assertFalse(passAllowed);
        assertEquals("/ride/passenger/login", passRes.getRedirectedUrl());

        // Unauthenticated driver route redirects to /ride/driver/login
        MockHttpServletRequest driverReq = new MockHttpServletRequest("GET", "/ride/driver");
        MockHttpServletResponse driverRes = new MockHttpServletResponse();
        boolean driverAllowed = authInterceptor.preHandle(driverReq, driverRes, new Object());
        assertFalse(driverAllowed);
        assertEquals("/ride/driver/login", driverRes.getRedirectedUrl());
    }

    @Test
    @DisplayName("3. API endpoints return 401 JSON for unauthenticated requests")
    void testApiEndpointsReturn401Json() throws Exception {
        MockHttpServletRequest apiReq = new MockHttpServletRequest("POST", "/ride/api/location/update");
        MockHttpServletResponse apiRes = new MockHttpServletResponse();
        boolean allowed = authInterceptor.preHandle(apiReq, apiRes, new Object());

        assertFalse(allowed);
        assertEquals(401, apiRes.getStatus());
        assertTrue(apiRes.getContentType().contains("application/json"));
        assertTrue(apiRes.getContentAsString().contains("\"status\":401"));
    }

    @Test
    @DisplayName("4. Passenger login verifies legacy plaintext password and seamlessly upgrades to BCrypt")
    void testPassengerLegacyPasswordUpgrade() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        req.setSession(session);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        // Initially password is plaintext
        assertFalse(passwordService.isBCrypt(passengerUser.getPassword()));

        String view = rideAuthController.processPassengerLogin(
                passengerUser.getEmail(), "plainPass123", req, session, redirectAttributes);

        assertEquals("redirect:/ride/passenger", view);
        assertEquals(passengerUser.getId(), ((User) session.getAttribute("loggedUser")).getId());

        // User record in DB must now have BCrypt hash
        User updatedUser = userRepository.findById(passengerUser.getId()).orElseThrow();
        assertTrue(passwordService.isBCrypt(updatedUser.getPassword()));
        assertTrue(passwordService.matches("plainPass123", updatedUser.getPassword()));
    }

    @Test
    @DisplayName("5. Atomic driver registration creates both User and linked Driver profile")
    void testAtomicDriverRegistration() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        req.setSession(session);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String email = "new_driver_" + System.currentTimeMillis() + "@test.com";
        String view = rideAuthController.processDriverRegister(
                "Kamal Silva", email, "secret123", "0712345678", "WP-CAB-9999", "Van",
                req, session, redirectAttributes);

        assertEquals("redirect:/ride/driver", view);

        User loggedUser = (User) session.getAttribute("loggedUser");
        Driver loggedDriver = (Driver) session.getAttribute("loggedDriver");
        assertNotNull(loggedUser);
        assertNotNull(loggedDriver);

        assertEquals("Kamal Silva", loggedUser.getFullName());
        assertEquals("Kamal Silva", loggedDriver.getDriverName());
        assertEquals("Van", loggedDriver.getVehicleType());
        assertEquals(loggedUser.getId(), loggedDriver.getUserId());
        assertTrue(passwordService.isBCrypt(loggedUser.getPassword()));
    }

    @Test
    @DisplayName("6. Driver login enforces linked driver profile; passenger without driver profile is rejected")
    void testDriverLoginEnforcesProfile() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        req.setSession(session);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        // Attempting to log into driver portal with a passenger account having no driver profile
        String view = rideAuthController.processDriverLogin(
                passengerUser.getEmail(), "plainPass123", req, session, redirectAttributes);

        assertEquals("redirect:/ride/driver/login?tab=register", view);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("errorMsg"));
        assertNull(session.getAttribute("loggedDriver"));
    }

    @Test
    @DisplayName("7. Safe legacy conflict handling: multiple profiles for one account triggers conflict status")
    void testLegacyMultipleProfilesConflictDetection() {
        // Add a second driver profile to driverUser
        Driver duplicateDriver = new Driver();
        duplicateDriver.setUserId(driverUser.getId());
        duplicateDriver.setDriverName("Test Driver Second Vehicle");
        duplicateDriver.setPhoneNumber("0771112222");
        duplicateDriver.setVehicleNumber("WP-DUP-2222");
        duplicateDriver.setVehicleType("Tuk");
        duplicateDriver.setStatus("AVAILABLE");
        driverRepository.save(duplicateDriver);

        DriverProfileService.DriverResolutionResult res = driverProfileService.resolveDriverProfile(driverUser.getId());
        assertEquals(DriverProfileService.ProfileResolutionStatus.MULTIPLE_PROFILES_CONFLICT, res.getStatus());
        assertNull(res.getDriver());
        assertEquals(2, res.getConflictingDrivers().size());
        assertTrue(res.getMessage().contains("Multiple driver profiles detected"));
    }

    @Test
    @DisplayName("8. Driver dashboard isolates data: shows only unassigned PENDING rides and personal trips")
    void testDriverDashboardDataIsolation() {
        // Create an unassigned PENDING ride
        Booking pendingRide = new Booking();
        pendingRide.setPassengerUser(passengerUser);
        pendingRide.setPassengerName("Passenger 1");
        pendingRide.setPickupLocation("Galle Face");
        pendingRide.setDropLocation("Bambalapitiya");
        pendingRide.setStatus("PENDING");
        pendingRide.setVehicleType("Car");
        pendingRide.setDriver(null);
        bookingRepository.save(pendingRide);

        // Create a ride assigned to THIS driver
        Booking myRide = new Booking();
        myRide.setPassengerUser(passengerUser);
        myRide.setPassengerName("Passenger 1");
        myRide.setPickupLocation("Kollupitiya");
        myRide.setDropLocation("Dehiwala");
        myRide.setStatus("ACCEPTED");
        myRide.setVehicleType("Car");
        myRide.setDriver(driverProfile);
        bookingRepository.save(myRide);

        // Create a ride assigned to a DIFFERENT driver
        Driver otherDriver = new Driver();
        otherDriver.setDriverName("Other Driver");
        otherDriver.setVehicleType("Car");
        otherDriver.setVehicleNumber("WP-OTH-3333");
        otherDriver = driverRepository.save(otherDriver);

        Booking otherDriverRide = new Booking();
        otherDriverRide.setPassengerUser(passengerUser);
        otherDriverRide.setPassengerName("Passenger 1");
        otherDriverRide.setPickupLocation("Negombo");
        otherDriverRide.setDropLocation("Katunayake");
        otherDriverRide.setStatus("ACCEPTED");
        otherDriverRide.setVehicleType("Car");
        otherDriverRide.setDriver(otherDriver);
        bookingRepository.save(otherDriverRide);

        // Load driver dashboard
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", driverUser);
        Model model = new ConcurrentModel();
        MockHttpServletResponse response = new MockHttpServletResponse();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = bookingController.driverView(model, session, response, redirectAttributes);
        assertEquals("ride/driver", view);

        @SuppressWarnings("unchecked")
        List<Booking> pendingBookings = (List<Booking>) model.getAttribute("pendingBookings");
        @SuppressWarnings("unchecked")
        List<Booking> myTrips = (List<Booking>) model.getAttribute("myTrips");

        assertNotNull(pendingBookings);
        assertNotNull(myTrips);

        // Eligible unassigned pending ride IS present
        assertTrue(pendingBookings.stream().anyMatch(b -> b.getId().equals(pendingRide.getId())));

        // My assigned ride IS present in personal trips
        assertTrue(myTrips.stream().anyMatch(b -> b.getId().equals(myRide.getId())));

        // Other driver's assigned ride IS NOT present in either list!
        assertFalse(pendingBookings.stream().anyMatch(b -> b.getId().equals(otherDriverRide.getId())));
        assertFalse(myTrips.stream().anyMatch(b -> b.getId().equals(otherDriverRide.getId())));
    }

    @Test
    @DisplayName("9. Driver can only toggle their own availability; other driver status modification is rejected")
    void testDriverAvailabilityIsolation() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", driverUser);

        // Toggle own status
        RedirectAttributesModelMap messages = new RedirectAttributesModelMap();
        bookingController.toggleOwnStatus(session, messages);

        Driver updatedDriver = driverRepository.findById(driverProfile.getId()).orElseThrow();
        assertEquals("BUSY", updatedDriver.getStatus());

        // Attempt to toggle another driver's status via /driver/toggle/{id}
        Driver otherDriver = new Driver();
        otherDriver.setDriverName("Other Driver");
        otherDriver.setStatus("AVAILABLE");
        otherDriver = driverRepository.save(otherDriver);

        MockHttpServletResponse errorResponse = new MockHttpServletResponse();
        String result = bookingController.toggleDriverStatus(otherDriver.getId(), session, errorResponse);
        assertNull(result);
        assertEquals(403, errorResponse.getStatus());
        assertEquals("AVAILABLE", driverRepository.findById(otherDriver.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("10. Driver cannot complete another driver's assigned ride")
    void testDriverCannotCompleteOtherDriverRide() throws Exception {
        Driver otherDriver = new Driver();
        otherDriver.setDriverName("Other Driver");
        otherDriver = driverRepository.save(otherDriver);

        Booking otherRide = new Booking();
        otherRide.setPassengerUser(passengerUser);
        otherRide.setPassengerName("Passenger");
        otherRide.setStatus("IN_PROGRESS");
        otherRide.setDriver(otherDriver);
        otherRide = bookingRepository.save(otherRide);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", driverUser);
        MockHttpServletResponse res = new MockHttpServletResponse();

        String result = bookingController.completeRide(otherRide.getId(), session, res);
        assertNull(result);
        assertEquals(403, res.getStatus());
        assertEquals("IN_PROGRESS", bookingRepository.findById(otherRide.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("11. Logout invalidates session and redirects to portal selection /ride/ride-hub")
    void testRideLogoutRedirectsToPortalSelection() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", passengerUser);

        String redirect = rideAuthController.rideLogout(session);
        assertEquals("redirect:/ride/ride-hub", redirect);
        assertTrue(session.isInvalid());
    }
}

package com.webtrans;

import com.webtrans.Courier.controller.CourierPageController;
import com.webtrans.Courier.dto.parcel.CancelRequest;
import com.webtrans.Courier.dto.parcel.CreateParcelRequest;
import com.webtrans.Courier.dto.parcel.ParcelResponse;
import com.webtrans.Courier.dto.partner.CreatePartnerRequest;
import com.webtrans.Courier.dto.partner.PartnerResponse;
import com.webtrans.Courier.model.CourierUser;
import com.webtrans.Courier.model.Parcel;
import com.webtrans.Courier.model.Partner;
import com.webtrans.Courier.repository.CourierUserRepository;
import com.webtrans.Courier.repository.ParcelRepository;
import com.webtrans.Courier.repository.PartnerRepository;
import com.webtrans.Courier.service.CourierProfileService;
import com.webtrans.Courier.service.ParcelService;
import com.webtrans.Courier.service.PartnerService;
import com.webtrans.auth.controller.AuthController;
import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CourierIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourierPageController courierPageController;

    @Autowired
    private com.webtrans.Courier.controller.CourierTrackingController courierTrackingController;

    @Autowired
    private CourierProfileService courierProfileService;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private ParcelService courierParcelService;

    @Autowired
    private CourierUserRepository courierUserRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private ParcelRepository courierParcelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthController authController;

    @Test
    @DisplayName("1. Shared Login -> Courier opens without second login and auto-provisions Courier profile")
    void testSharedLoginAutoProvisionsProfile() {
        // Create shared passenger user
        User sharedUser = new User();
        sharedUser.setFullName("Saman Perera");
        sharedUser.setEmail("saman_" + System.currentTimeMillis() + "@test.com");
        sharedUser.setPassword("password123");
        sharedUser.setRole("PASSENGER");
        sharedUser = userRepository.save(sharedUser);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", sharedUser);

        // Access /courier
        String redirect = courierPageController.index(session);
        assertEquals("redirect:/courier/sender", redirect);

        // Verify courier_users record was provisioned with shared_user_id
        CourierUser courierProfile = courierUserRepository.findBySharedUserId(sharedUser.getId()).orElse(null);
        assertNotNull(courierProfile);
        assertEquals(sharedUser.getEmail(), courierProfile.getEmail());
        assertEquals("Saman Perera", courierProfile.getName());
        assertEquals(CourierUser.Role.SENDER, courierProfile.getRole());
        assertNull(courierProfile.getPasswordHash(), "No password required for shared-login provisioned profile");

        // Verify sender view renders
        ConcurrentModel model = new ConcurrentModel();
        String view = courierPageController.sender(session, model);
        assertEquals("courier/sender", view);
        assertEquals("Saman Perera", model.getAttribute("courierUserName"));
        assertEquals("SENDER", model.getAttribute("courierUserRole"));
    }

    @Test
    @DisplayName("2. Unauthenticated entry redirects to shared login with safe return destination")
    void testUnauthenticatedEntryRedirectsToSharedLogin() {
        MockHttpSession emptySession = new MockHttpSession();

        // /courier entry unauthenticated
        String redirect = courierPageController.index(emptySession);
        assertEquals("redirect:/login?redirect=/courier", redirect);

        // Old /courier/login URL redirect
        assertEquals("redirect:/login?redirect=/courier", courierPageController.login());

        // Old /courier/register URL redirect
        assertEquals("redirect:/ride/home?loginRequired=true&redirect=/courier", courierPageController.register());

        // AuthController loginPage forwards with encoded safe return destination
        String loginReturn = authController.loginPage("/courier", emptySession);
        assertTrue(loginReturn.contains("loginRequired=true"));
        assertTrue(loginReturn.contains("redirect="));
    }

    @Test
    @DisplayName("3. Safe return destination validation prevents open redirects and redirect loops")
    void testSafeRedirectValidation() {
        // Safe internal URLs allowed
        assertEquals("/courier", AuthController.sanitizeRedirect("/courier"));
        assertEquals("/courier/sender", AuthController.sanitizeRedirect("/courier/sender"));
        assertEquals("/ride/passenger", AuthController.sanitizeRedirect("/ride/passenger"));

        // Malicious open redirect attempts blocked
        assertNull(AuthController.sanitizeRedirect("//evil.com"));
        assertNull(AuthController.sanitizeRedirect("/\\evil.com"));
        assertNull(AuthController.sanitizeRedirect("https://evil.com"));

        // Redirect loops blocked
        assertNull(AuthController.sanitizeRedirect("/login"));
        assertNull(AuthController.sanitizeRedirect("/logout"));
        assertNull(AuthController.sanitizeRedirect("/courier/login"));
    }

    @Test
    @DisplayName("4. Ambiguous match resolution: existing legacy Courier account prompts password verification")
    void testAmbiguousLegacyAccountRequiresPasswordVerification() {
        String email = "legacy_user_" + System.currentTimeMillis() + "@test.com";

        // Pre-existing unlinked legacy Courier account
        CourierUser legacyCourierUser = CourierUser.builder()
                .name("Legacy Sender")
                .email(email)
                .passwordHash(passwordEncoder.encode("courierOldPass123"))
                .role(CourierUser.Role.SENDER)
                .sharedUserId(null)
                .build();
        legacyCourierUser = courierUserRepository.save(legacyCourierUser);

        // New shared user with the same email
        User sharedUser = new User();
        sharedUser.setFullName("Shared User");
        sharedUser.setEmail(email);
        sharedUser.setPassword("newPass456");
        sharedUser.setRole("PASSENGER");
        sharedUser = userRepository.save(sharedUser);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", sharedUser);

        // Visiting /courier encounters ambiguous match and routes to link-account
        String redirect = courierPageController.index(session);
        assertEquals("redirect:/courier/link-account", redirect);

        // Attempting to link with wrong password fails and preserves account unlinked
        ConcurrentModel model = new ConcurrentModel();
        String failView = courierPageController.processLinkAccount("wrongPassword", session, model);
        assertEquals("courier/link-account", failView);
        assertNotNull(model.getAttribute("error"));
        CourierUser stillUnlinked = courierUserRepository.findByEmail(email).orElseThrow();
        assertNull(stillUnlinked.getSharedUserId(), "Must NOT link on invalid password");

        // Attempting to link with correct legacy password succeeds
        String successRedirect = courierPageController.processLinkAccount("courierOldPass123", session, model);
        assertEquals("redirect:/courier", successRedirect);

        // Account is now linked to shared user ID
        CourierUser nowLinked = courierUserRepository.findByEmail(email).orElseThrow();
        assertEquals(sharedUser.getId(), nowLinked.getSharedUserId());

        // Subsequent entry opens Courier seamlessly
        assertEquals("redirect:/courier/sender", courierPageController.index(session));
    }

    @Test
    @DisplayName("5. Delivery Partner onboarding under shared account without separate login")
    void testPartnerOnboardingUnderSharedAccount() {
        User sharedUser = new User();
        sharedUser.setFullName("Kamal Partner");
        sharedUser.setEmail("kamal_" + System.currentTimeMillis() + "@test.com");
        sharedUser.setPassword("pass123");
        sharedUser = userRepository.save(sharedUser);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", sharedUser);

        // Onboard as partner with vehicle type and phone
        ConcurrentModel model = new ConcurrentModel();
        String result = courierPageController.processRegisterPartner("VAN", "0771234567", session, model);
        assertEquals("redirect:/courier/partner", result);

        // Verify partner status
        CourierUser courierUser = courierUserRepository.findBySharedUserId(sharedUser.getId()).orElseThrow();
        assertEquals(CourierUser.Role.PARTNER, courierUser.getRole());
        assertEquals("0771234567", courierUser.getPhone());

        Partner partner = partnerRepository.findByUserId(courierUser.getId()).orElseThrow();
        assertEquals(Partner.VehicleType.VAN, partner.getVehicleType());
        assertTrue(partner.getIsAvailable());
        assertFalse(partner.getIsOnline());

        // Visiting /courier now directs to partner view
        assertEquals("redirect:/courier/partner", courierPageController.index(session));

        // Partner can also view sender dashboard
        assertEquals("courier/sender", courierPageController.sender(session, new ConcurrentModel()));
    }

    @Test
    @DisplayName("6. Role enforcement: ordinary user cannot access partner dashboard without onboarding")
    void testOrdinaryUserCannotAccessPartnerDashboard() {
        User senderUser = new User();
        senderUser.setFullName("Regular Sender");
        senderUser.setEmail("regular_" + System.currentTimeMillis() + "@test.com");
        senderUser.setPassword("pass123");
        senderUser = userRepository.save(senderUser);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", senderUser);

        // Accessing partner page redirects to partner onboarding
        String result = courierPageController.partner(session, new ConcurrentModel());
        assertEquals("redirect:/courier/register-partner", result);
    }

    @Test
    @DisplayName("7. Parcel ownership isolation: users only access their own parcels")
    void testParcelOwnershipIsolation() {
        String email1 = "sender1_" + System.currentTimeMillis() + "@test.com";
        String email2 = "sender2_" + System.currentTimeMillis() + "@test.com";

        CourierUser user1 = courierUserRepository.save(CourierUser.builder()
                .name("Sender One").email(email1).role(CourierUser.Role.SENDER).build());
        CourierUser user2 = courierUserRepository.save(CourierUser.builder()
                .name("Sender Two").email(email2).role(CourierUser.Role.SENDER).build());

        CreateParcelRequest req = new CreateParcelRequest();
        req.setReceiverName("Receiver Test");
        req.setReceiverEmail("rcv@test.com");
        req.setPickupAddress("Location A");
        req.setPickupLat(BigDecimal.valueOf(6.9271));
        req.setPickupLng(BigDecimal.valueOf(79.8612));
        req.setDropAddress("Location B");
        req.setDropLat(BigDecimal.valueOf(6.9344));
        req.setDropLng(BigDecimal.valueOf(79.8443));
        req.setWeightKg(BigDecimal.valueOf(1.5));
        req.setSize(Parcel.Size.MEDIUM);
        req.setServiceTier(Parcel.ServiceTier.INSTANT);

        ParcelResponse parcel1 = courierParcelService.createParcel(req, email1);

        // User 2 cannot view User 1's parcel
        RuntimeException viewEx = assertThrows(RuntimeException.class, () -> courierParcelService.getById(parcel1.getId(), email2));
        assertTrue(viewEx.getMessage().toLowerCase().contains("not authorized"));

        // User 2 cannot cancel User 1's parcel
        CancelRequest cancelReq = new CancelRequest(List.of("WRONG_ADDRESS"), "Not my parcel");
        RuntimeException cancelEx = assertThrows(RuntimeException.class, () -> courierParcelService.cancelParcel(parcel1.getId(), cancelReq, email2));
        assertTrue(cancelEx.getMessage().toLowerCase().contains("not authorized"));

        // User 1 can successfully view and cancel their own parcel
        ParcelResponse own = courierParcelService.getById(parcel1.getId(), email1);
        assertEquals(parcel1.getId(), own.getId());
        ParcelResponse cancelled = courierParcelService.cancelParcel(parcel1.getId(), cancelReq, email1);
        assertEquals(Parcel.Status.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("8. Courier logout clears shared session while preserving independent driver and admin sessions")
    void testCourierLogoutPreservesIndependentDriverAndAdmin() {
        User sharedUser = new User();
        sharedUser.setFullName("Multi Session User");
        sharedUser.setEmail("multi_" + System.currentTimeMillis() + "@test.com");
        sharedUser.setPassword("pass123");
        sharedUser = userRepository.save(sharedUser);

        User driverUser = new User();
        driverUser.setFullName("Driver Jack");
        driverUser.setEmail("driver_jack@test.com");
        driverUser.setRole("DRIVER");

        User adminUser = new User();
        adminUser.setFullName("Admin Boss");
        adminUser.setEmail("admin_boss@test.com");
        adminUser.setRole("ADMIN");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", sharedUser);
        session.setAttribute("userRole", "PASSENGER");
        session.setAttribute("courierUser", CourierUser.builder().id(999L).email(sharedUser.getEmail()).build());
        session.setAttribute("driverUser", driverUser);
        session.setAttribute("adminUser", adminUser);
        session.setAttribute("adminRole", "ADMIN");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);

        // Perform Courier logout
        String redirect = courierPageController.logout(session, request);
        assertEquals("redirect:/ride/home", redirect);

        // Home/Passenger/Courier identity removed
        assertNull(session.getAttribute("loggedUser"));
        assertNull(session.getAttribute("userRole"));
        assertNull(session.getAttribute("courierUser"));

        // Independent Driver and Admin sessions preserved
        assertNotNull(session.getAttribute("driverUser"));
        assertEquals("driver_jack@test.com", ((User) session.getAttribute("driverUser")).getEmail());
        assertNotNull(session.getAttribute("adminUser"));
        assertEquals("ADMIN", session.getAttribute("adminRole"));
    }

    @Test
    @DisplayName("9. Driver-only or Admin-only session cannot access Courier customer functions")
    void testDriverOnlyOrAdminOnlyCannotAccessCourier() {
        MockHttpSession driverSession = new MockHttpSession();
        User driver = new User();
        driver.setEmail("driver_only@test.com");
        driverSession.setAttribute("driverUser", driver);

        // Driver without loggedUser is treated as unauthenticated for Courier
        String redirect = courierPageController.index(driverSession);
        assertEquals("redirect:/login?redirect=/courier", redirect);

        MockHttpSession adminSession = new MockHttpSession();
        User admin = new User();
        admin.setEmail("admin_only@test.com");
        adminSession.setAttribute("adminUser", admin);
        adminSession.setAttribute("adminRole", "ADMIN");

        // Admin without loggedUser is treated as unauthenticated for Courier
        String adminRedirect = courierPageController.index(adminSession);
        assertEquals("redirect:/login?redirect=/courier", adminRedirect);
    }

    @Test
    @DisplayName("10. Unauthenticated tracking page request redirects to shared login preserving return URL and code")
    void testUnauthenticatedTrackingPageRedirectsToLogin() {
        MockHttpSession emptySession = new MockHttpSession();

        // Direct /courier/track without code
        String redirect1 = courierPageController.track(null, null, emptySession, new ConcurrentModel());
        assertTrue(redirect1.startsWith("redirect:/login?redirect="));
        assertTrue(redirect1.contains("%2Fcourier%2Ftrack"));

        // Direct /courier/track with tracking code CR-9A4B2C
        String redirect2 = courierPageController.track("CR-9A4B2C", null, emptySession, new ConcurrentModel());
        assertTrue(redirect2.startsWith("redirect:/login?redirect="));
        assertTrue(redirect2.contains("CR-9A4B2C"));

        // Driver-only session is treated as unauthenticated for customer tracking
        MockHttpSession driverSession = new MockHttpSession();
        User driver = new User();
        driver.setEmail("driver@test.com");
        driverSession.setAttribute("driverUser", driver);
        String redirectDriver = courierPageController.track("CR-9A4B2C", null, driverSession, new ConcurrentModel());
        assertTrue(redirectDriver.startsWith("redirect:/login?redirect="));
    }

    @Test
    @DisplayName("11. Authenticated user opens tracking page with prefilled code")
    void testAuthenticatedTrackingPageOpensWithPrefilledCode() {
        User sharedUser = new User();
        sharedUser.setFullName("Dinuja Track");
        sharedUser.setEmail("dinuja_track_" + System.currentTimeMillis() + "@test.com");
        sharedUser.setPassword("pass123");
        sharedUser.setRole("PASSENGER");
        sharedUser = userRepository.save(sharedUser);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", sharedUser);

        ConcurrentModel model = new ConcurrentModel();
        String view = courierPageController.track("CR-ABC123", null, session, model);
        assertEquals("courier/track", view);
        assertEquals("CR-ABC123", model.getAttribute("initialTrackingCode"));
        assertEquals("Dinuja Track", model.getAttribute("courierUserName"));
    }

    @Test
    @DisplayName("12. Tracking API requires shared loggedUser session; blocks unauthenticated, driver-only, and admin-only")
    void testTrackingApiSecurity() {
        MockHttpSession emptySession = new MockHttpSession();
        var resp1 = courierTrackingController.track("CR-ANY123", emptySession);
        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, resp1.getStatusCode());

        MockHttpSession driverSession = new MockHttpSession();
        User driver = new User();
        driver.setEmail("driver_only@test.com");
        driverSession.setAttribute("driverUser", driver);
        var resp2 = courierTrackingController.track("CR-ANY123", driverSession);
        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, resp2.getStatusCode());

        MockHttpSession adminSession = new MockHttpSession();
        User admin = new User();
        admin.setEmail("admin_only@test.com");
        adminSession.setAttribute("adminUser", admin);
        adminSession.setAttribute("adminRole", "ADMIN");
        var resp3 = courierTrackingController.track("CR-ANY123", adminSession);
        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, resp3.getStatusCode());
    }

    @Test
    @DisplayName("13. Signed-in recipient or sender can track parcel by code without exposing delivery OTP")
    void testTrackingByCodeAccessAndOtpProtection() {
        // Creator creates parcel
        String senderEmail = "sender_" + System.currentTimeMillis() + "@test.com";
        courierUserRepository.save(CourierUser.builder()
                .name("Sender Person").email(senderEmail).role(CourierUser.Role.SENDER).build());

        CreateParcelRequest req = new CreateParcelRequest();
        req.setReceiverName("Recipient Dinuja");
        req.setReceiverEmail("recipient@test.com");
        req.setPickupAddress("Colombo 03");
        req.setPickupLat(BigDecimal.valueOf(6.9034));
        req.setPickupLng(BigDecimal.valueOf(79.8540));
        req.setDropAddress("Colombo 07");
        req.setDropLat(BigDecimal.valueOf(6.9110));
        req.setDropLng(BigDecimal.valueOf(79.8650));
        req.setWeightKg(BigDecimal.valueOf(2.0));
        req.setSize(Parcel.Size.MEDIUM);
        req.setServiceTier(Parcel.ServiceTier.INSTANT);

        ParcelResponse created = courierParcelService.createParcel(req, senderEmail);
        String trackingCode = created.getTrackingCode();
        assertNotNull(trackingCode);
        assertTrue(trackingCode.startsWith("CR-"));

        // Recipient (different user) logs in with shared account
        User recipientUser = new User();
        recipientUser.setFullName("Recipient Dinuja");
        recipientUser.setEmail("recipient@test.com");
        recipientUser.setPassword("pass123");
        recipientUser.setRole("PASSENGER");
        recipientUser = userRepository.save(recipientUser);

        MockHttpSession recipientSession = new MockHttpSession();
        recipientSession.setAttribute("loggedUser", recipientUser);

        // Recipient tracks parcel using shareable tracking code
        var trackResp = courierTrackingController.track(trackingCode, recipientSession);
        assertEquals(org.springframework.http.HttpStatus.OK, trackResp.getStatusCode());

        com.webtrans.Courier.dto.parcel.TrackingResponse data =
                (com.webtrans.Courier.dto.parcel.TrackingResponse) trackResp.getBody();
        assertNotNull(data);
        assertEquals(trackingCode, data.getTrackingCode());
        assertEquals("Recipient", data.getReceiverFirstName());
        assertEquals("Colombo 03", data.getPickupAddress());
        assertEquals("Colombo 07", data.getDropAddress());
        // Verify delivery OTP is NOT exposed in tracking response
        assertNull(data.getDeliveryOtp(), "Delivery OTP must remain hidden in tracking-by-code response");
    }

    @Test
    @DisplayName("14. Tracking API returns 404 for non-existent tracking code")
    void testTrackingApiReturns404ForUnknownCode() {
        User user = new User();
        user.setFullName("Tester");
        user.setEmail("tester_" + System.currentTimeMillis() + "@test.com");
        user.setPassword("pass123");
        user.setRole("PASSENGER");
        user = userRepository.save(user);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", user);

        var resp = courierTrackingController.track("CR-NOTEXIST", session);
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    @DisplayName("15. POST /api/courier/parcels succeeds without 403 Forbidden under shared login session")
    void testPostApiCourierParcelsSucceedsWithSharedSession() throws Exception {
        User sharedUser = new User();
        sharedUser.setFullName("Dinuja Sender");
        sharedUser.setEmail("dinuja_sender_" + System.currentTimeMillis() + "@test.com");
        sharedUser.setPassword("pass123");
        sharedUser.setRole("PASSENGER");
        sharedUser = userRepository.save(sharedUser);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", sharedUser);

        String jsonPayload = """
            {
              "receiverName": "Nimal Perera",
              "receiverEmail": "nimal@test.com",
              "receiverPhone": "0771122334",
              "pickupAddress": "Colombo 03",
              "pickupLat": 6.9225,
              "pickupLng": 79.9775,
              "dropAddress": "Colombo 07",
              "dropLat": 6.9837,
              "dropLng": 79.9410,
              "weightKg": 2.5,
              "size": "SMALL",
              "serviceTier": "INSTANT"
            }
            """;

        mockMvc.perform(post("/api/courier/parcels")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trackingCode").isNotEmpty())
                .andExpect(jsonPath("$.receiverName").value("Nimal Perera"));
    }
}

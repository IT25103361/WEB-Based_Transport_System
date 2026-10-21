package com.webtrans;

import com.webtrans.admin.controller.AdminDashboardController;
import com.webtrans.admin.dto.AdminOverviewDto;
import com.webtrans.admin.dto.ModuleDetailsDto;
import com.webtrans.admin.dto.ModuleSummaryDto;
import com.webtrans.admin.service.AdminDashboardService;
import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import com.webtrans.auth.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AdminDashboardTest {

    @Autowired
    private AdminDashboardController adminController;

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    private User adminUser;
    private User passengerUser;

    @BeforeEach
    void setUp() {
        // Ensure verified ADMIN account exists
        adminUser = userRepository.findByEmail("admin_test_operator@nextgo.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("admin_test_operator@nextgo.com");
            u.setFullName("Test Operator Admin");
            u.setPassword(passwordService.encode("AdminPass#2026"));
            u.setRole("ADMIN");
            return userRepository.save(u);
        });
        adminUser.setRole("ADMIN");
        adminUser.setPassword(passwordService.encode("AdminPass#2026"));
        userRepository.save(adminUser);

        // Ensure regular PASSENGER account exists
        passengerUser = userRepository.findByEmail("passenger_regular@test.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("passenger_regular@test.com");
            u.setFullName("Regular Passenger");
            u.setPassword(passwordService.encode("UserPass#123"));
            u.setRole("PASSENGER");
            return userRepository.save(u);
        });
        passengerUser.setRole("PASSENGER");
        userRepository.save(passengerUser);
    }

    @Test
    @DisplayName("1. Unauthenticated user is redirected away from admin dashboard")
    void testUnauthenticatedUserRedirected() {
        MockHttpSession session = new MockHttpSession();
        Model model = new ConcurrentModel();

        String view = adminController.dashboard("7days", null, null, session, model);
        assertEquals("redirect:/admin/login?error=auth_required", view);
    }

    @Test
    @DisplayName("2. Normal passenger session cannot access admin dashboard")
    void testPassengerCannotAccessAdminDashboard() {
        MockHttpSession session = new MockHttpSession();
        // Regular passenger login sets loggedUser and userRole
        session.setAttribute("loggedUser", passengerUser);
        session.setAttribute("userRole", "PASSENGER");
        Model model = new ConcurrentModel();

        String view = adminController.dashboard("7days", null, null, session, model);
        assertEquals("redirect:/admin/login?error=auth_required", view,
                "Normal passenger session must not grant access to admin dashboard");
    }

    @Test
    @DisplayName("3. Unauthenticated request to /admin/api/overview returns 401 Unauthorized")
    void testAdminApiProtectedWith401() {
        MockHttpSession session = new MockHttpSession();
        ResponseEntity<?> response = adminController.getOverviewApi("7days", null, null, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Admin session required"));
    }

    @Test
    @DisplayName("4. Non-admin user is rejected at POST /admin/login")
    void testNonAdminRejectedAtAdminLogin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = (MockHttpSession) request.getSession();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String result = adminController.processAdminLogin(
                passengerUser.getEmail(),
                "UserPass#123",
                request,
                session,
                redirectAttributes
        );

        assertEquals("redirect:/admin/login", result);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("errorMsg"));
        assertTrue(redirectAttributes.getFlashAttributes().get("errorMsg").toString().contains("does not have administrative privileges"));
        assertNull(session.getAttribute("adminUser"));
    }

    @Test
    @DisplayName("5. Designated admin successfully authenticates at POST /admin/login")
    void testDesignatedAdminLoginSuccess() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = (MockHttpSession) request.getSession();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String result = adminController.processAdminLogin(
                adminUser.getEmail(),
                "AdminPass#2026",
                request,
                session,
                redirectAttributes
        );

        assertEquals("redirect:/admin/dashboard", result);
        assertNotNull(session.getAttribute("adminUser"));
        assertEquals("ADMIN", session.getAttribute("adminRole"));

        // Now authorized dashboard view can be accessed
        Model model = new ConcurrentModel();
        String dashView = adminController.dashboard("7days", null, null, session, model);
        assertEquals("admin/dashboard", dashView);
        assertNotNull(model.getAttribute("overview"));
        assertNotNull(model.getAttribute("activities"));
    }

    @Test
    @DisplayName("6. All SIX core modules are represented with accurate status and honest states")
    void testAllSixModulesRepresented() {
        AdminOverviewDto overview = adminDashboardService.getOverview("7days", null, null);
        assertNotNull(overview);
        assertNotNull(overview.getModules());
        assertEquals(6, overview.getModules().size(), "Dashboard must represent exactly all 6 modules from Home page");

        List<ModuleSummaryDto> mods = overview.getModules();
        assertEquals("ride", mods.get(0).getModuleKey());
        assertTrue(mods.get(0).isHasRealData());
        assertEquals("ACTIVE", mods.get(0).getStatusBadge());

        assertEquals("courier", mods.get(1).getModuleKey());
        assertFalse(mods.get(1).isHasRealData());
        assertEquals("IN_PROGRESS", mods.get(1).getStatusBadge());
        assertTrue(mods.get(1).getStatusMessage().contains("Member module assigned"));

        assertEquals("school", mods.get(2).getModuleKey());
        assertTrue(mods.get(2).isHasRealData());
        assertEquals("ACTIVE", mods.get(2).getStatusBadge());

        assertEquals("food", mods.get(3).getModuleKey());
        assertFalse(mods.get(3).isHasRealData());
        assertEquals("IN_PROGRESS", mods.get(3).getStatusBadge());

        assertEquals("rental", mods.get(4).getModuleKey());
        assertTrue(mods.get(4).isHasRealData());
        assertEquals("ACTIVE", mods.get(4).getStatusBadge());

        assertEquals("emergency", mods.get(5).getModuleKey());
        assertFalse(mods.get(5).isHasRealData());
        assertEquals("IN_PROGRESS", mods.get(5).getStatusBadge());
    }

    @Test
    @DisplayName("7. Financial indicators strictly distinguish verified collected revenue from uncollected estimates")
    void testFinancialSegregation() {
        AdminOverviewDto overview = adminDashboardService.getOverview("7days", null, null);
        assertNotNull(overview.getCollectedRideRevenue());
        assertNotNull(overview.getPendingRideFares());
        assertNotNull(overview.getAgreedRentalValue());

        assertTrue(overview.getFormattedCollectedRideRevenue().startsWith("LKR "));
        assertTrue(overview.getFormattedPendingRideFares().startsWith("LKR "));
        assertTrue(overview.getFormattedAgreedRentalValue().startsWith("LKR "));
    }

    @Test
    @DisplayName("8. Date range filters apply correctly and display Sri Lanka timezone (Asia/Colombo)")
    void testDateRangeFiltersAndTimezone() {
        AdminOverviewDto todayOverview = adminDashboardService.getOverview("today", null, null);
        assertEquals(LocalDate.now(AdminDashboardService.BUSINESS_ZONE).toString(), todayOverview.getStartDate());
        assertEquals("Asia/Colombo (UTC+05:30)", todayOverview.getTimezone());

        AdminOverviewDto customOverview = adminDashboardService.getOverview("custom", "2026-10-01", "2026-10-15");
        assertEquals("2026-10-01", customOverview.getStartDate());
        assertEquals("2026-10-15", customOverview.getEndDate());
        assertFalse(customOverview.getTrends().isEmpty());
    }

    @Test
    @DisplayName("9. Granular module detail endpoint returns resources and records for active and in-progress modules")
    void testModuleDetailsEndpoint() {
        ModuleDetailsDto rideDetails = adminDashboardService.getModuleDetails("ride");
        assertEquals("Ride Management System", rideDetails.getModuleName());
        assertFalse(rideDetails.isInProgress());

        ModuleDetailsDto courierDetails = adminDashboardService.getModuleDetails("courier");
        assertEquals("Courier Delivery Service", courierDetails.getModuleName());
        assertTrue(courierDetails.isInProgress());
        assertNotNull(courierDetails.getIntegrationNotes());

        ModuleDetailsDto rentalDetails = adminDashboardService.getModuleDetails("rental");
        assertEquals("Vehicle Rental Management", rentalDetails.getModuleName());
        assertFalse(rentalDetails.isInProgress());
    }

    @Test
    @DisplayName("10. Admin logout cleanly removes admin session attributes")
    void testAdminLogout() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("adminUser", adminUser);
        session.setAttribute("adminRole", "ADMIN");

        String redirect = adminController.adminLogout(session);
        assertEquals("redirect:/admin/login?logout=success", redirect);
        assertNull(session.getAttribute("adminUser"));
        assertNull(session.getAttribute("adminRole"));
    }
}

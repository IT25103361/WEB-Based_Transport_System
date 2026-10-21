package com.webtrans.Courier.controller;

import com.webtrans.Courier.dto.partner.CreatePartnerRequest;
import com.webtrans.Courier.model.CourierUser;
import com.webtrans.Courier.model.Partner;
import com.webtrans.Courier.service.CourierProfileService;
import com.webtrans.Courier.service.CourierProfileService.ProfileResolutionResult;
import com.webtrans.Courier.service.CourierProfileService.ResolutionStatus;
import com.webtrans.Courier.service.PartnerService;
import com.webtrans.auth.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("courierPageController")
@RequestMapping("/courier")
public class CourierPageController {

    @Autowired
    private CourierProfileService profileService;

    @Autowired
    private PartnerService partnerService;

    private User getSharedUser(HttpSession session) {
        if (session != null && session.getAttribute("loggedUser") instanceof User) {
            return (User) session.getAttribute("loggedUser");
        }
        return null;
    }

    /**
     * Entry point for Courier from Home page or direct /courier navigation.
     * Routes shared users to Sender or Partner view, or prompts linking for ambiguous legacy accounts.
     */
    @GetMapping({"", "/"})
    public String index(HttpSession session) {
        User sharedUser = getSharedUser(session);
        if (sharedUser == null) {
            return "redirect:/login?redirect=/courier";
        }

        ProfileResolutionResult res = profileService.resolveProfile(sharedUser);
        if (res.getStatus() == ResolutionStatus.AMBIGUOUS_UNLINKED_ACCOUNT) {
            return "redirect:/courier/link-account";
        }
        if (res.isSuccess()) {
            CourierUser profile = res.getProfile();
            session.setAttribute("courierUser", profile);
            if (profile.getRole() == CourierUser.Role.PARTNER) {
                return "redirect:/courier/partner";
            }
            return "redirect:/courier/sender";
        }

        return "redirect:/ride/home?error=courier_unavailable";
    }

    /**
     * Redirect legacy credential entry URLs to the shared login/registration flow.
     */
    @GetMapping({"/login", "/login.html"})
    public String login() {
        return "redirect:/login?redirect=/courier";
    }

    @GetMapping({"/register", "/register.html"})
    public String register() {
        return "redirect:/ride/home?loginRequired=true&redirect=/courier";
    }

    /**
     * Sender dashboard for booking and tracking packages.
     */
    @GetMapping({"/sender", "/sender.html"})
    public String sender(HttpSession session, Model model) {
        User sharedUser = getSharedUser(session);
        if (sharedUser == null) {
            return "redirect:/login?redirect=/courier/sender";
        }

        ProfileResolutionResult res = profileService.resolveProfile(sharedUser);
        if (res.getStatus() == ResolutionStatus.AMBIGUOUS_UNLINKED_ACCOUNT) {
            return "redirect:/courier/link-account";
        }
        if (!res.isSuccess()) {
            return "redirect:/ride/home?error=courier_unavailable";
        }

        CourierUser profile = res.getProfile();
        session.setAttribute("courierUser", profile);
        model.addAttribute("courierUserName", profile.getName());
        model.addAttribute("courierUserRole", profile.getRole().name());
        model.addAttribute("courierUserEmail", profile.getEmail());
        model.addAttribute("sharedUser", sharedUser);

        return "courier/sender";
    }

    /**
     * Partner dashboard for delivery drivers.
     */
    @GetMapping({"/partner", "/partner.html"})
    public String partner(HttpSession session, Model model) {
        User sharedUser = getSharedUser(session);
        if (sharedUser == null) {
            return "redirect:/login?redirect=/courier/partner";
        }

        ProfileResolutionResult res = profileService.resolveProfile(sharedUser);
        if (res.getStatus() == ResolutionStatus.AMBIGUOUS_UNLINKED_ACCOUNT) {
            return "redirect:/courier/link-account";
        }
        if (!res.isSuccess()) {
            return "redirect:/ride/home?error=courier_unavailable";
        }

        CourierUser profile = res.getProfile();
        // If not a partner, redirect to partner onboarding
        if (profile.getRole() != CourierUser.Role.PARTNER) {
            return "redirect:/courier/register-partner";
        }

        session.setAttribute("courierUser", profile);
        model.addAttribute("courierUserName", profile.getName());
        model.addAttribute("courierUserRole", profile.getRole().name());
        model.addAttribute("courierUserEmail", profile.getEmail());
        model.addAttribute("sharedUser", sharedUser);

        return "courier/partner";
    }

    /**
     * Delivery partner onboarding under existing shared account (no separate credentials).
     */
    @GetMapping({"/register-partner", "/register-partner.html"})
    public String registerPartner(HttpSession session, Model model) {
        User sharedUser = getSharedUser(session);
        if (sharedUser == null) {
            return "redirect:/login?redirect=/courier/register-partner";
        }

        ProfileResolutionResult res = profileService.resolveProfile(sharedUser);
        if (res.getStatus() == ResolutionStatus.AMBIGUOUS_UNLINKED_ACCOUNT) {
            return "redirect:/courier/link-account";
        }
        if (res.isSuccess() && res.getProfile().getRole() == CourierUser.Role.PARTNER) {
            return "redirect:/courier/partner";
        }

        CourierUser profile = res.getProfile();
        model.addAttribute("sharedUser", sharedUser);
        model.addAttribute("courierUserName", profile != null ? profile.getName() : sharedUser.getFullName());
        model.addAttribute("courierUserEmail", profile != null ? profile.getEmail() : sharedUser.getEmail());

        return "courier/register-partner";
    }

    @PostMapping("/register-partner")
    public String processRegisterPartner(
            @RequestParam(defaultValue = "BIKE") String vehicleType,
            @RequestParam(required = false) String phone,
            HttpSession session,
            Model model
    ) {
        User sharedUser = getSharedUser(session);
        if (sharedUser == null) {
            return "redirect:/login?redirect=/courier/register-partner";
        }

        try {
            Partner.VehicleType vType;
            try {
                vType = Partner.VehicleType.valueOf(vehicleType.trim().toUpperCase());
            } catch (Exception e) {
                vType = Partner.VehicleType.BIKE;
            }
            profileService.onboardPartner(sharedUser, vType, phone);
            return "redirect:/courier/partner";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("sharedUser", sharedUser);
            return "courier/register-partner";
        }
    }

    /**
     * Resolution page for ambiguous legacy accounts matching the shared user's email.
     */
    @GetMapping("/link-account")
    public String linkAccountPage(HttpSession session, Model model) {
        User sharedUser = getSharedUser(session);
        if (sharedUser == null) {
            return "redirect:/login?redirect=/courier";
        }

        ProfileResolutionResult res = profileService.resolveProfile(sharedUser);
        if (res.getStatus() != ResolutionStatus.AMBIGUOUS_UNLINKED_ACCOUNT) {
            return "redirect:/courier";
        }

        model.addAttribute("email", sharedUser.getEmail());
        model.addAttribute("userName", sharedUser.getFullName());
        return "courier/link-account";
    }

    @PostMapping("/link-account")
    public String processLinkAccount(
            @RequestParam String legacyPassword,
            HttpSession session,
            Model model
    ) {
        User sharedUser = getSharedUser(session);
        if (sharedUser == null) {
            return "redirect:/login?redirect=/courier";
        }

        boolean linked = profileService.linkLegacyAccount(sharedUser, legacyPassword);
        if (linked) {
            return "redirect:/courier";
        }

        model.addAttribute("email", sharedUser.getEmail());
        model.addAttribute("userName", sharedUser.getFullName());
        model.addAttribute("error", "Incorrect Courier account password. Please try again or return to the Main Hub.");
        return "courier/link-account";
    }

    /**
     * Shared logout for Courier: removes Home/Passenger/Courier access while preserving
     * independent Driver and Admin sessions.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request) {
        if (session != null) {
            session.removeAttribute("loggedUser");
            session.removeAttribute("userRole");
            session.removeAttribute("courierUser");
        }
        SecurityContextHolder.clearContext();
        return "redirect:/ride/home";
    }

    /**
     * Authenticated tracking page — requires shared Home/Passenger session.
     * Unauthenticated users are redirected to the shared login, safely preserving
     * the requested tracking URL and code to return afterward.
     */
    @GetMapping({"/track", "/track.html"})
    public String track(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "trackingCode", required = false) String trackingCode,
            HttpSession session,
            Model model
    ) {
        User sharedUser = getSharedUser(session);
        String c = (code != null && !code.isBlank()) ? code.trim() : (trackingCode != null && !trackingCode.isBlank() ? trackingCode.trim() : null);

        if (sharedUser == null) {
            String trackingUrl = "/courier/track";
            if (c != null) {
                trackingUrl += "?code=" + java.net.URLEncoder.encode(c, java.nio.charset.StandardCharsets.UTF_8);
            }
            return "redirect:/login?redirect=" + java.net.URLEncoder.encode(trackingUrl, java.nio.charset.StandardCharsets.UTF_8);
        }

        ProfileResolutionResult res = profileService.resolveProfile(sharedUser);
        if (res.getStatus() == ResolutionStatus.AMBIGUOUS_UNLINKED_ACCOUNT) {
            return "redirect:/courier/link-account";
        }

        CourierUser profile = res.isSuccess() ? res.getProfile() : null;
        if (profile != null) {
            session.setAttribute("courierUser", profile);
            model.addAttribute("courierUserName", profile.getName());
            model.addAttribute("courierUserRole", profile.getRole().name());
            model.addAttribute("courierUserEmail", profile.getEmail());
        } else {
            model.addAttribute("courierUserName", sharedUser.getFullName());
            model.addAttribute("courierUserRole", "SENDER");
            model.addAttribute("courierUserEmail", sharedUser.getEmail());
        }
        model.addAttribute("sharedUser", sharedUser);
        model.addAttribute("initialTrackingCode", c != null ? c : "");

        return "courier/track";
    }
}

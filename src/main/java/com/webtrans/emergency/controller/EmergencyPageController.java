package com.webtrans.emergency.controller;

import com.webtrans.auth.model.User;
import com.webtrans.emergency.dto.EmergencyTrackingResponseDto;
import com.webtrans.emergency.service.EmergencyCsrfService;
import com.webtrans.emergency.service.EmergencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class EmergencyPageController {

    @Autowired
    private EmergencyCsrfService csrfService;

    @Autowired
    private EmergencyService emergencyService;

    /**
     * Public Emergency Request Portal.
     * Works without login or registration for citizens in distress.
     */
    @GetMapping({"/emergency", "/emergency/"})
    public String index(HttpSession session, Model model) {
        String token = csrfService.getOrCreateToken(session);
        model.addAttribute("csrfToken", token);
        return "emergency/index";
    }

    /**
     * Public Tracking Page.
     * Viewable by anyone possessing their unguessable tracking token.
     */
    @GetMapping("/emergency/track")
    public String track(
            @RequestParam(name = "token", required = false) String token,
            HttpSession session,
            Model model
    ) {
        String csrfToken = csrfService.getOrCreateToken(session);
        model.addAttribute("csrfToken", csrfToken);
        if (token != null && !token.isBlank()) {
            Optional<EmergencyTrackingResponseDto> record = emergencyService.getTrackingByToken(token.trim());
            if (record.isPresent()) {
                model.addAttribute("incident", record.get());
                model.addAttribute("found", true);
            } else {
                model.addAttribute("found", false);
                model.addAttribute("searchToken", token.trim());
            }
        }
        return "emergency/track";
    }

    /**
     * Emergency Operational Admin Console.
     * Accessible only to authenticated Administrators.
     */
    @GetMapping("/emergency/admin")
    public String adminConsole(HttpSession session, HttpServletRequest request) {
        if (session == null) {
            return "redirect:/admin/login?error=auth_required";
        }
        Object adminUser = session.getAttribute("adminUser");
        Object adminRole = session.getAttribute("adminRole");
        if (!(adminUser instanceof User) || !"ADMIN".equals(adminRole)) {
            return "redirect:/admin/login?error=auth_required";
        }
        return "emergency/admin";
    }
}

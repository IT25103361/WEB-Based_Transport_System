package com.webtrans.auth.controller;

import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.webtrans.auth.service.PasswordService passwordService;

    public static String sanitizeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return null;
        }
        String clean = redirect.trim();
        // Prevent protocol-relative URL or Windows separator evasion
        if (!clean.startsWith("/") || clean.startsWith("//") || clean.startsWith("/\\")) {
            return null;
        }
        // Prevent redirect loops
        if (clean.equals("/login") || clean.startsWith("/login?") ||
            clean.equals("/logout") || clean.startsWith("/logout?") ||
            clean.equals("/courier/login") || clean.startsWith("/courier/login?") ||
            clean.equals("/food/login") || clean.startsWith("/food/login?")) {
            return null;
        }
        // Allow internal application paths only
        if (clean.startsWith("/courier") || clean.startsWith("/ride") ||
            clean.startsWith("/school") || clean.startsWith("/rental") ||
            clean.startsWith("/admin") || clean.startsWith("/food") ||
            clean.equals("/") || clean.startsWith("/?")) {
            return clean;
        }
        return null;
    }

    // Redirect generic /login to home page (triggering login modal if unauthenticated)
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String redirect, HttpSession session) {
        String safe = sanitizeRedirect(redirect);
        if (safe != null) {
            return "redirect:/ride/home?loginRequired=true&redirect=" + java.net.URLEncoder.encode(safe, java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/ride/home?loginRequired=true";
    }

    // Handle Login
    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               @RequestParam(required = false) String redirect,
                               jakarta.servlet.http.HttpServletRequest request,
                               HttpSession session,
                               Model model) {
        String normalizedEmail = (email != null) ? email.trim().toLowerCase() : "";
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isPresent() && passwordService.matches(password, userOpt.get().getPassword())) {
            User user = userOpt.get();
            passwordService.upgradeIfLegacy(user, password);
            if (request.getSession(false) != null) {
                request.changeSessionId();
            }
            session.setAttribute("loggedUser", user);
            session.setAttribute("userRole", "PASSENGER");

            String safe = sanitizeRedirect(redirect);
            if (safe != null) {
                return "redirect:" + safe;
            }
            return "redirect:/ride/home";
        }

        String errRedirect = "/ride/home?error=invalid";
        String safe = sanitizeRedirect(redirect);
        if (safe != null) {
            errRedirect += "&redirect=" + java.net.URLEncoder.encode(safe, java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:" + errRedirect;
    }

    // Handle Registration
    @PostMapping("/register")
    public String processRegister(@RequestParam String fullName,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  @RequestParam(required = false) String redirect,
                                  jakarta.servlet.http.HttpServletRequest request,
                                  HttpSession session,
                                  Model model) {
        String normalizedEmail = (email != null) ? email.trim().toLowerCase() : "";
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            String errRedirect = "/ride/home?regError=exists";
            String safe = sanitizeRedirect(redirect);
            if (safe != null) {
                errRedirect += "&redirect=" + java.net.URLEncoder.encode(safe, java.nio.charset.StandardCharsets.UTF_8);
            }
            return "redirect:" + errRedirect;
        }

        User newUser = new User();
        newUser.setFullName(fullName != null ? fullName.trim() : "");
        newUser.setEmail(normalizedEmail);
        newUser.setPassword(passwordService.encode(password));
        User savedUser = userRepository.save(newUser);

        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        session.setAttribute("loggedUser", savedUser);
        session.setAttribute("userRole", "PASSENGER");

        String safe = sanitizeRedirect(redirect);
        if (safe != null) {
            return "redirect:" + safe;
        }
        return "redirect:/ride/home";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Home, Passenger, and Courier share this identity; preserve independent driver and admin logins.
        session.removeAttribute("loggedUser");
        session.removeAttribute("userRole");
        session.removeAttribute("courierUser");
        return "redirect:/ride/home";
    }

    // Unified Home Page / Hub (Login at top if not logged in)
    @GetMapping("/")
    public String hubPage(HttpSession session, Model model) {
        return "redirect:/ride/home";
    }

    // Direct shortcut to Ride Hub
    @GetMapping("/ride-hub")
    public String rideHubDirect() {
        return "redirect:/ride/ride-hub";
    }
}

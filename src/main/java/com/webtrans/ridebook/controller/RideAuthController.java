package com.webtrans.ridebook.controller;

import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import com.webtrans.auth.service.PasswordService;
import com.webtrans.ridebook.model.Driver;
import com.webtrans.ridebook.service.DriverProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/ride")
public class RideAuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private DriverProfileService driverProfileService;

    // =========================================================================
    // PASSENGER AUTHENTICATION ENTRY FLOW
    // =========================================================================

    @GetMapping("/passenger/login")
    public String passengerLoginPage(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null) {
            return "redirect:/ride/passenger";
        }
        return "ride/passenger-login";
    }

    @PostMapping("/passenger/login")
    public String processPassengerLogin(@RequestParam String email,
                                        @RequestParam String password,
                                        HttpServletRequest request,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        String normalizedEmail = (email != null) ? email.trim().toLowerCase() : "";
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        if (userOpt.isPresent() && passwordService.matches(password, userOpt.get().getPassword())) {
            User user = userOpt.get();
            // Transparently upgrade legacy plaintext password to BCrypt
            passwordService.upgradeIfLegacy(user, password);

            // Rotate session ID to protect against session fixation
            if (request.getSession(false) != null) {
                request.changeSessionId();
            }

            session.setAttribute("loggedUser", user);
            session.setAttribute("userRole", "PASSENGER");
            return "redirect:/ride/passenger";
        }

        redirectAttributes.addFlashAttribute("errorMsg", "Invalid email or password.");
        return "redirect:/ride/passenger/login";
    }

    @PostMapping("/passenger/register")
    public String processPassengerRegister(@RequestParam String fullName,
                                           @RequestParam String email,
                                           @RequestParam String password,
                                           HttpServletRequest request,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        if (fullName == null || fullName.isBlank() ||
                email == null || email.isBlank() ||
                password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", "All fields are required.");
            return "redirect:/ride/passenger/login?tab=register";
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMsg", "An account with this email already exists.");
            return "redirect:/ride/passenger/login?tab=register";
        }

        User newUser = new User();
        newUser.setFullName(fullName.trim());
        newUser.setEmail(normalizedEmail);
        newUser.setPassword(passwordService.encode(password));
        User savedUser = userRepository.save(newUser);

        // Rotate session ID
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        session.setAttribute("loggedUser", savedUser);
        session.setAttribute("userRole", "PASSENGER");
        return "redirect:/ride/passenger";
    }

    // =========================================================================
    // DRIVER AUTHENTICATION ENTRY FLOW
    // =========================================================================

    @GetMapping("/driver/login")
    public String driverLoginPage(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("driverUser");
        if (loggedUser != null) {
            DriverProfileService.DriverResolutionResult res = driverProfileService.resolveDriverProfile(loggedUser.getId());
            if (res.getStatus() == DriverProfileService.ProfileResolutionStatus.SUCCESS) {
                return "redirect:/ride/driver";
            }
        }
        return "ride/driver-login";
    }

    @PostMapping("/driver/login")
    public String processDriverLogin(@RequestParam String email,
                                     @RequestParam String password,
                                     HttpServletRequest request,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String normalizedEmail = (email != null) ? email.trim().toLowerCase() : "";
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        if (userOpt.isEmpty() || !passwordService.matches(password, userOpt.get().getPassword())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid email or password.");
            return "redirect:/ride/driver/login";
        }

        User user = userOpt.get();
        passwordService.upgradeIfLegacy(user, password);

        // Enforce driver profile authorization on the server
        DriverProfileService.DriverResolutionResult res = driverProfileService.resolveDriverProfile(user.getId());
        if (res.getStatus() == DriverProfileService.ProfileResolutionStatus.NO_PROFILE) {
            redirectAttributes.addFlashAttribute("errorMsg", "No driver profile linked to this account. Please register your driver profile below.");
            return "redirect:/ride/driver/login?tab=register";
        }

        if (res.getStatus() == DriverProfileService.ProfileResolutionStatus.MULTIPLE_PROFILES_CONFLICT) {
            redirectAttributes.addFlashAttribute("errorMsg", res.getMessage());
            return "redirect:/ride/driver/login";
        }

        // Successfully resolved exactly one linked driver profile
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        session.removeAttribute("rideSelectedDriverId");
        session.setAttribute("driverUser", user);
        session.setAttribute("loggedDriver", res.getDriver());

        return "redirect:/ride/driver";
    }

    @PostMapping("/driver/register")
    public String processDriverRegister(@RequestParam String fullName,
                                        @RequestParam String email,
                                        @RequestParam String password,
                                        @RequestParam String phoneNumber,
                                        @RequestParam String vehicleNumber,
                                        @RequestParam String vehicleType,
                                        HttpServletRequest request,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        try {
            // Atomically creates User + Driver profile
            Driver driver = driverProfileService.registerDriver(fullName, email, password, phoneNumber, vehicleNumber, vehicleType);
            User user = userRepository.findById(driver.getUserId()).orElseThrow();

            if (request.getSession(false) != null) {
                request.changeSessionId();
            }

            session.removeAttribute("rideSelectedDriverId");
            session.setAttribute("driverUser", user);
            session.setAttribute("loggedDriver", driver);
            session.setAttribute("loggedUser", user);

            return "redirect:/ride/driver";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/ride/driver/login?tab=register";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMsg", "Registration failed. Please verify your details and try again.");
            return "redirect:/ride/driver/login?tab=register";
        }
    }

    // =========================================================================
    // LOGOUT (Returns to Ride Portal Selection)
    // =========================================================================

    // Passenger logout invalidates the user session and returns to ride hub.
    @GetMapping({"/logout", "/passenger/logout"})
    public String rideLogout(HttpSession session) {
        session.removeAttribute("loggedUser");
        session.removeAttribute("userRole");
        session.invalidate();
        return "redirect:/ride/ride-hub";
    }

    // Driver logout strictly ends only the active driver session, leaving global user session intact.
    @GetMapping("/driver/logout")
    public String driverLogout(HttpSession session) {
        session.removeAttribute("driverUser");
        session.removeAttribute("loggedDriver");
        session.removeAttribute("rideSelectedDriverId");
        return "redirect:/ride/ride-hub";
    }
}

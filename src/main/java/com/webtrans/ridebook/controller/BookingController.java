package com.webtrans.ridebook.controller;

import com.webtrans.ridebook.service.DriverProfileService;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.webtrans.ridebook.model.Booking;
import com.webtrans.ridebook.model.Driver;
import com.webtrans.ridebook.repository.BookingRepository;
import com.webtrans.ridebook.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.webtrans.auth.model.User;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ride")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private DriverProfileService driverProfiles;

    @Autowired
    private com.webtrans.ridebook.strategy.RideFareCalculator rideFareCalculator;

    @GetMapping({"/", "/home"})
    public String home(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null) {
            model.addAttribute("user", loggedUser);
        }
        return "ride/home";
    }

    @GetMapping("/ride-hub")
    public String rideHub() {
        return "ride/ride-hub";
    }

    @GetMapping("/passenger")
    public String passengerView(Model model, HttpSession session, HttpServletResponse response) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return "redirect:/ride/passenger/login";
        }

        // Prevent browser caching across logout and account switching
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        Booking booking = new Booking();
        booking.setPassengerName(loggedUser.getFullName());
        model.addAttribute("booking", booking);

        // Server-side filtering: fetch only bookings belonging to the authenticated user's ID
        List<Booking> bookings = bookingRepository.findByPassengerUser_IdOrderByIdDesc(loggedUser.getId());
        model.addAttribute("bookings", bookings != null ? bookings : new ArrayList<>());
        return "ride/passenger";
    }

    // Fare quotation API using Strategy Pattern
    @GetMapping("/api/fare/quote")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quoteFare(
            @RequestParam(name = "vehicleType", required = false, defaultValue = "Car") String vehicleType,
            @RequestParam(name = "distanceKm", required = false) Double distanceKm,
            @RequestParam(name = "pickupLat", required = false) Double pickupLat,
            @RequestParam(name = "pickupLng", required = false) Double pickupLng,
            @RequestParam(name = "dropLat", required = false) Double dropLat,
            @RequestParam(name = "dropLng", required = false) Double dropLng) {

        double dist = (distanceKm != null && distanceKm > 0) ? distanceKm : 1.0;
        if (distanceKm == null && pickupLat != null && pickupLng != null && dropLat != null && dropLng != null) {
            dist = calculateDistanceKm(pickupLat, pickupLng, dropLat, dropLng);
        }

        double fare = rideFareCalculator.calculateFare(vehicleType, dist);
        Map<String, Double> allFares = rideFareCalculator.calculateAllFares(dist);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("vehicleType", vehicleType);
        resp.put("distanceKm", dist);
        resp.put("fare", fare);
        resp.put("allFares", allFares);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/book")
    public String createBooking(@ModelAttribute Booking booking, HttpSession session, HttpServletResponse response) throws IOException {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to create a booking.");
            return null;
        }

        // Enforce ownership strictly from authenticated session
        booking.setPassengerUser(loggedUser);

        // Keep passengerName purely as a display field; names are not unique and never determine ownership
        if (booking.getPassengerName() == null || booking.getPassengerName().trim().isEmpty()) {
            booking.setPassengerName(loggedUser.getFullName());
        }

        // Enforce strategy pattern fare calculation in production booking flow
        if (booking.getVehicleType() != null) {
            double distKm = 1.0;
            if (booking.getPickupLat() != null && booking.getPickupLng() != null
                    && booking.getDropLat() != null && booking.getDropLng() != null) {
                distKm = calculateDistanceKm(booking.getPickupLat(), booking.getPickupLng(),
                        booking.getDropLat(), booking.getDropLng());
            }
            double calculatedFare = rideFareCalculator.calculateFare(booking.getVehicleType(), distKm);
            if (booking.getFare() == null || booking.getFare() <= 0) {
                booking.setFare(calculatedFare);
            }
        }

        booking.setStatus("PENDING");
        booking.setDriver(null);
        bookingRepository.save(booking);
        return "redirect:/ride/passenger";
    }

    // Passenger cancels their booking
    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, HttpSession session, HttpServletResponse response) throws IOException {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to cancel a booking.");
            return null;
        }

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
            return null;
        }

        // Verify ownership: unowned legacy bookings and other passengers' bookings cannot be cancelled
        if (booking.getPassengerUser() == null || !loggedUser.getId().equals(booking.getPassengerUser().getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: You do not own this booking.");
            return null;
        }

        try {
            if ("PENDING".equalsIgnoreCase(booking.getStatus()) || "ACCEPTED".equalsIgnoreCase(booking.getStatus())) {
                Driver driver = booking.getDriver();
                if (driver != null) {
                    driver.setStatus("AVAILABLE");
                    driverRepository.save(driver);
                }

                booking.setStatus("CANCELLED");
                booking.setDriver(null);
                bookingRepository.save(booking);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ride/passenger";
    }

    public static final double MAX_PROXIMITY_KM = 5.0;

    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static boolean isWithinProximity(Driver driver, Booking booking, double radiusKm) {
        if (driver == null || booking == null) {
            return false;
        }
        // If driver has a live location and booking has pickup coordinates, enforce proximity filter
        if (driver.getCurrentLat() != null && driver.getCurrentLng() != null
                && booking.getPickupLat() != null && booking.getPickupLng() != null) {
            double distance = calculateDistanceKm(
                    driver.getCurrentLat(), driver.getCurrentLng(),
                    booking.getPickupLat(), booking.getPickupLng()
            );
            return distance <= radiusKm;
        }
        // If driver has live location but booking has no coordinates, cannot confirm within radius
        if (driver.getCurrentLat() != null && driver.getCurrentLng() != null) {
            return false;
        }
        // Fallback for drivers or test fixtures without GPS coordinates yet
        return true;
    }

    private User resolveDriverUser(HttpSession session) {
        if (session == null) return null;
        User driverUser = (User) session.getAttribute("driverUser");
        if (driverUser != null) {
            return driverUser;
        }
        User fallback = (User) session.getAttribute("loggedUser");
        if (fallback != null && driverProfiles.resolveDriverProfile(fallback.getId()).getStatus() == DriverProfileService.ProfileResolutionStatus.SUCCESS) {
            return fallback;
        }
        return null;
    }

    @GetMapping("/driver")
    public String driverView(Model model, HttpSession session, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        User loggedUser = resolveDriverUser(session);
        if (loggedUser == null) {
            return "redirect:/ride/driver/login";
        }

        // Prevent browser caching across logout and account switching
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        DriverProfileService.DriverResolutionResult res = driverProfiles.resolveDriverProfile(loggedUser.getId());
        if (res.getStatus() == DriverProfileService.ProfileResolutionStatus.NO_PROFILE) {
            redirectAttributes.addFlashAttribute("errorMsg", "No driver profile linked to this account. Please register your driver profile below.");
            return "redirect:/ride/driver/login?tab=register";
        }
        if (res.getStatus() == DriverProfileService.ProfileResolutionStatus.MULTIPLE_PROFILES_CONFLICT) {
            redirectAttributes.addFlashAttribute("errorMsg", res.getMessage());
            return "redirect:/ride/driver/login";
        }

        Driver driver = res.getDriver();
        session.setAttribute("loggedDriver", driver);


        model.addAttribute("driver", driver);
        model.addAttribute("currentDriver", driver);

        // Retain the eligible unassigned PENDING ride requests within 5 KM radius of driver's live location.
        List<Booking> allPending = bookingRepository.findByStatusAndDriverIsNullOrderByIdDesc("PENDING");
        List<Booking> pendingBookings = new ArrayList<>();
        if (allPending != null) {
            for (Booking b : allPending) {
                if (isWithinProximity(driver, b, MAX_PROXIMITY_KM)) {
                    pendingBookings.add(b);
                }
            }
        }
        model.addAttribute("pendingBookings", pendingBookings);

        // Show the logged-in driver's own details, availability, assigned/current rides and ride history only.
        List<Booking> myTrips = bookingRepository.findByDriver_IdOrderByIdDesc(driver.getId());
        model.addAttribute("myTrips", myTrips != null ? myTrips : new ArrayList<>());

        return "ride/driver";
    }

    @PostMapping("/driver/toggle-status")
    public String toggleOwnStatus(HttpSession session, RedirectAttributes messages) {
        User loggedUser = resolveDriverUser(session);
        if (loggedUser == null) return "redirect:/ride/driver/login";

        DriverProfileService.DriverResolutionResult res = driverProfiles.resolveDriverProfile(loggedUser.getId());
        if (res.getStatus() != DriverProfileService.ProfileResolutionStatus.SUCCESS) {
            return "redirect:/ride/driver/login";
        }

        Driver driver = res.getDriver();
        if ("AVAILABLE".equalsIgnoreCase(driver.getStatus())) {
            driver.setStatus("BUSY");
        } else {
            driver.setStatus("AVAILABLE");
        }
        driverRepository.save(driver);
        session.setAttribute("loggedDriver", driver);
        messages.addFlashAttribute("successMsg", "Your availability status is now " + driver.getStatus() + ".");
        return "redirect:/ride/driver";
    }

    @GetMapping("/driver/toggle-status")
    public String toggleOwnStatusGet(HttpSession session, RedirectAttributes messages) {
        return toggleOwnStatus(session, messages);
    }

    @GetMapping("/driver/toggle/{id}")
    public String toggleDriverStatus(@PathVariable Long id, HttpSession session, HttpServletResponse response) throws IOException {
        User loggedUser = resolveDriverUser(session);
        if (loggedUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Driver must be authenticated.");
            return null;
        }

        DriverProfileService.DriverResolutionResult res = driverProfiles.resolveDriverProfile(loggedUser.getId());
        if (res.getStatus() != DriverProfileService.ProfileResolutionStatus.SUCCESS || !res.getDriver().getId().equals(id)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: You can only change your own availability.");
            return null;
        }

        Driver driver = res.getDriver();
        if ("AVAILABLE".equalsIgnoreCase(driver.getStatus())) {
            driver.setStatus("BUSY");
        } else {
            driver.setStatus("AVAILABLE");
        }
        driverRepository.save(driver);
        session.setAttribute("loggedDriver", driver);
        return "redirect:/ride/driver";
    }

    @PostMapping("/accept/{id}")
    @Transactional
    public String acceptRide(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedUser = resolveDriverUser(session);
        if (loggedUser == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "You must be logged in to accept a ride.");
            return "redirect:/ride/driver/login";
        }

        try {
            DriverProfileService.DriverResolutionResult res = driverProfiles.resolveDriverProfile(loggedUser.getId());
            if (res.getStatus() != DriverProfileService.ProfileResolutionStatus.SUCCESS) {
                redirectAttributes.addFlashAttribute("errorMsg", "No valid driver profile resolved for your account.");
                return "redirect:/ride/driver/login";
            }
            Driver driver = res.getDriver();
            if (!"AVAILABLE".equalsIgnoreCase(driver.getStatus())) {
                redirectAttributes.addFlashAttribute("errorMsg", "You cannot accept this ride because your status is " + driver.getStatus() + ". Please complete your active ride first.");
                return "redirect:/ride/driver";
            }

            Booking booking = bookingRepository.findById(id).orElse(null);

            if (booking != null) {
                if (!"PENDING".equalsIgnoreCase(booking.getStatus()) || booking.getDriver() != null) {
                    redirectAttributes.addFlashAttribute("errorMsg", "This booking is no longer available.");
                    return "redirect:/ride/driver";
                }

                if (!isWithinProximity(driver, booking, MAX_PROXIMITY_KM)) {
                    redirectAttributes.addFlashAttribute("errorMsg", "Cannot accept ride: Pickup location is outside your 5 KM service radius.");
                    return "redirect:/ride/driver";
                }

                String bType = booking.getVehicleType() != null ? booking.getVehicleType().toLowerCase() : "";
                String dType = driver.getVehicleType() != null ? driver.getVehicleType().toLowerCase() : "";

                // Normalize "Wheel" and "Three-Wheeler" to "tuk" for comparison
                if (bType.contains("wheel")) bType = "tuk";
                if (dType.contains("wheel")) dType = "tuk";

                if (!bType.equals(dType) && !bType.isEmpty() && !dType.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMsg", "Vehicle type mismatch. Passenger requested " + booking.getVehicleType() + " but you drive a " + driver.getVehicleType() + ".");
                    return "redirect:/ride/driver";
                }

                booking.setDriver(driver);
                booking.setStatus("ACCEPTED");
                bookingRepository.save(booking);

                driver.setStatus("BUSY");
                driverRepository.save(driver);
                session.setAttribute("loggedDriver", driver);

                redirectAttributes.addFlashAttribute("successMsg", "Ride accepted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Booking not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMsg", "An error occurred while accepting the ride.");
        }

        return "redirect:/ride/driver";
    }

    @GetMapping("/complete/{id}")
    public String completeRide(@PathVariable Long id, HttpSession session, HttpServletResponse response) throws IOException {
        User loggedUser = resolveDriverUser(session);
        if (loggedUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Driver must be authenticated.");
            return null;
        }

        DriverProfileService.DriverResolutionResult res = driverProfiles.resolveDriverProfile(loggedUser.getId());
        if (res.getStatus() != DriverProfileService.ProfileResolutionStatus.SUCCESS) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No valid driver profile resolved.");
            return null;
        }

        try {
            Booking booking = bookingRepository.findById(id).orElse(null);
            if (booking == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
                return null;
            }

            Driver driver = res.getDriver();
            if (booking.getDriver() == null || !driver.getId().equals(booking.getDriver().getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Only the assigned driver can complete this ride.");
                return null;
            }

            booking.setStatus("COMPLETED");
            bookingRepository.save(booking);

            driver.setStatus("AVAILABLE");
            driverRepository.save(driver);
            session.setAttribute("loggedDriver", driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ride/driver";
    }

    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable Long id, HttpSession session, HttpServletResponse response) throws IOException {
        User loggedUser = resolveDriverUser(session);
        if (loggedUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return null;
        }

        DriverProfileService.DriverResolutionResult res = driverProfiles.resolveDriverProfile(loggedUser.getId());
        if (res.getStatus() != DriverProfileService.ProfileResolutionStatus.SUCCESS) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
            return null;
        }

        try {
            Booking booking = bookingRepository.findById(id).orElse(null);
            if (booking != null) {
                if (booking.getDriver() != null && booking.getDriver().getId().equals(res.getDriver().getId())) {
                    bookingRepository.deleteById(id);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: You can only delete your own trips.");
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ride/driver";
    }

    @GetMapping("/payment/{id}")
    public String paymentPage(@PathVariable Long id, Model model, HttpSession session, HttpServletResponse response) throws IOException {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to view payment.");
            return null;
        }

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
            return null;
        }

        // Strict passenger ownership check
        if (booking.getPassengerUser() == null || !loggedUser.getId().equals(booking.getPassengerUser().getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: You do not own this booking.");
            return null;
        }

        if ("COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            model.addAttribute("booking", booking);
            return "ride/payment";
        }
        return "redirect:/ride/passenger";
    }

    @PostMapping("/pay/{id}")
    public String processPayment(@PathVariable Long id,
                                 @RequestParam(name = "appliedOffer", required = false) String appliedOffer,
                                 @RequestParam(name = "finalFare", required = false) Double finalFare,
                                 HttpSession session,
                                 HttpServletResponse response) throws IOException {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to process payment.");
            return null;
        }

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
            return null;
        }

        // Strict passenger ownership check
        if (booking.getPassengerUser() == null || !loggedUser.getId().equals(booking.getPassengerUser().getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: You do not own this booking.");
            return null;
        }

        try {
            if ("COMPLETED".equalsIgnoreCase(booking.getStatus())) {
                booking.setStatus("PAID");
                if (appliedOffer != null) booking.setAppliedOffer(appliedOffer);
                if (finalFare != null) booking.setFinalFare(finalFare);
                bookingRepository.save(booking);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ride/passenger";
    }

    @PostMapping("/review/{id}")
    public String submitReview(@PathVariable Long id,
                               @RequestParam(name = "rating", required = false) Integer rating,
                               @RequestParam(name = "reviewComment", required = false) String reviewComment,
                               HttpSession session,
                               HttpServletResponse response) throws IOException {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to submit a review.");
            return null;
        }

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
            return null;
        }

        // Strict passenger ownership check
        if (booking.getPassengerUser() == null || !loggedUser.getId().equals(booking.getPassengerUser().getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: You do not own this booking.");
            return null;
        }

        try {
            if (rating != null) booking.setRating(rating);
            if (reviewComment != null) booking.setReviewComment(reviewComment.trim());
            bookingRepository.save(booking);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ride/passenger";
    }
}
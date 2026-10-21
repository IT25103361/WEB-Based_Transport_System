package com.webtrans.ridebook.controller;

import com.webtrans.ridebook.service.DriverProfileService;
import com.webtrans.auth.model.User;
import com.webtrans.ridebook.model.Booking;
import com.webtrans.ridebook.model.Driver;
import com.webtrans.ridebook.repository.BookingRepository;
import com.webtrans.ridebook.repository.DriverRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ride/api")
public class TrackingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private DriverProfileService driverProfiles;

    // Driver updates location
    @PostMapping("/location/update")
    public ResponseEntity<?> updateLocation(@RequestBody Map<String, Double> payload, HttpSession session) {
        User loggedUser = (User) session.getAttribute("driverUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        DriverProfileService.DriverResolutionResult res = driverProfiles.resolveDriverProfile(loggedUser.getId());
        if (res.getStatus() != DriverProfileService.ProfileResolutionStatus.SUCCESS) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not registered as driver");
        }
        Driver driver = res.getDriver();

        Double lat = payload.get("lat");
        Double lng = payload.get("lng");
        if (lat == null || lng == null) {
            return ResponseEntity.badRequest().body("Missing lat or lng");
        }

        driver.setCurrentLat(lat);
        driver.setCurrentLng(lng);
        driver.setLastLocationUpdate(LocalDateTime.now());
        driverRepository.save(driver);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    // Passenger or Driver fetches tracking info
    @GetMapping("/tracking/{bookingId}")
    public ResponseEntity<?> getTrackingInfo(@PathVariable Long bookingId, HttpSession session) {
        User passengerUser = (User) session.getAttribute("loggedUser");
        User driverUser = (User) session.getAttribute("driverUser");
        if (passengerUser == null && driverUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }

        boolean isPassenger = passengerUser != null && booking.getPassengerUser() != null
                && booking.getPassengerUser().getId().equals(passengerUser.getId());
        boolean isDriver = (driverUser != null && booking.getDriver() != null
                && booking.getDriver().getUserId() != null
                && booking.getDriver().getUserId().equals(driverUser.getId()))
                || (passengerUser != null && booking.getDriver() != null
                && booking.getDriver().getUserId() != null
                && booking.getDriver().getUserId().equals(passengerUser.getId()));

        if (!isPassenger && !isDriver) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("bookingStatus", booking.getStatus());
        response.put("pickupLat", booking.getPickupLat());
        response.put("pickupLng", booking.getPickupLng());
        response.put("dropLat", booking.getDropLat());
        response.put("dropLng", booking.getDropLng());

        Driver driver = booking.getDriver();
        if (driver != null) {
            response.put("driverName", driver.getDriverName());
            response.put("vehicleNumber", driver.getVehicleNumber());
            response.put("vehicleType", driver.getVehicleType());
            response.put("driverPhone", driver.getPhoneNumber());

            if (driver.getCurrentLat() != null && driver.getCurrentLng() != null) {
                response.put("driverLat", driver.getCurrentLat());
                response.put("driverLng", driver.getCurrentLng());
                response.put("lastLocationUpdate", driver.getLastLocationUpdate() == null ? null : driver.getLastLocationUpdate().toString());
            }
        }

        return ResponseEntity.ok(response);
    }

    // Update booking status from driver
    @PostMapping("/booking/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long bookingId, @RequestBody Map<String, String> payload, HttpSession session) {
        User loggedUser = (User) session.getAttribute("driverUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }

        DriverProfileService.DriverResolutionResult res = driverProfiles.resolveDriverProfile(loggedUser.getId());
        if (res.getStatus() != DriverProfileService.ProfileResolutionStatus.SUCCESS) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only assigned driver can update status");
        }
        Driver driver = res.getDriver();

        if (booking.getDriver() == null || !driver.getId().equals(booking.getDriver().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only assigned driver can update status");
        }

        String status = payload.get("status");
        if ("ARRIVED".equals(status) || "IN_PROGRESS".equals(status)) {
            booking.setStatus(status);
            bookingRepository.save(booking);
            return ResponseEntity.ok(Map.of("status", "success"));
        }

        return ResponseEntity.badRequest().body("Invalid status transition");
    }
}

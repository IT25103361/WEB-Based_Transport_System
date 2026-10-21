package com.webtrans.Courier.controller;

import com.webtrans.Courier.service.TrackingService;
import com.webtrans.auth.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("courierTrackingController")
@RequestMapping("/api/courier/track")
public class CourierTrackingController {

    @Autowired
    private TrackingService trackingService;

    /**
     * Authenticated tracking-by-code.
     * Requires active shared Home/Passenger session.
     * GET /api/courier/track/{trackingCode}
     */
    @GetMapping("/{trackingCode}")
    public ResponseEntity<?> track(@PathVariable String trackingCode, HttpSession session) {
        if (session == null || !(session.getAttribute("loggedUser") instanceof User)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required to track parcels. Please sign in."));
        }

        try {
            return ResponseEntity.ok(trackingService.track(trackingCode));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

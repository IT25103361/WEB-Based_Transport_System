package com.seproject.courier.controller;

import com.seproject.courier.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/courier/track")
public class TrackingController {

    @Autowired
    private TrackingService trackingService;

    /**
     * Public tracking — no login required.
     * GET /api/courier/track/{trackingCode}
     */
    @GetMapping("/{trackingCode}")
    public ResponseEntity<?> track(@PathVariable String trackingCode) {
        try {
            return ResponseEntity.ok(trackingService.track(trackingCode));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
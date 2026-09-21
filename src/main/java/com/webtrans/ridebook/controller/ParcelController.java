package com.seproject.courier.controller;

import com.seproject.courier.dto.parcel.CancelRequest;
import com.seproject.courier.dto.parcel.CreateParcelRequest;
import com.seproject.courier.dto.parcel.ParcelResponse;
import com.seproject.courier.dto.parcel.PayRequest;
import com.seproject.courier.dto.parcel.PaymentResponse;
import com.seproject.courier.service.ParcelService;
import com.seproject.courier.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courier/parcels")
public class ParcelController {

    @Autowired private ParcelService parcelService;
    @Autowired private PaymentService paymentService;

    /**
     * Create a new parcel.
     * POST /api/courier/parcels
     * Auth: SENDER
     */
    @PostMapping
    public ResponseEntity<?> createParcel(
            @Valid @RequestBody CreateParcelRequest request,
            Authentication authentication
    ) {
        try {
            String senderEmail = authentication.getName();
            ParcelResponse response = parcelService.createParcel(request, senderEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all my parcels.
     * GET /api/courier/parcels/my
     * Auth: SENDER
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyParcels(Authentication authentication) {
        try {
            String senderEmail = authentication.getName();
            List<ParcelResponse> list = parcelService.getMyParcels(senderEmail);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get a single parcel by id.
     * GET /api/courier/parcels/{id}
     * Auth: SENDER (own parcels only)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            ParcelResponse response = parcelService.getById(id, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get the status timeline of a parcel.
     * GET /api/courier/parcels/{id}/timeline
     * Auth: SENDER (own parcels only)
     */
    @GetMapping("/{id}/timeline")
    public ResponseEntity<?> getTimeline(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(
                    parcelService.getTimeline(id, authentication.getName())
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== CANCEL ====================

    /**
     * Cancel a parcel before pickup.
     * POST /api/courier/parcels/{id}/cancel
     * Auth: SENDER (owner only)
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelRequest request,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(
                    parcelService.cancelParcel(id, request, authentication.getName())
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== PAYMENTS ====================

    /**
     * Simulate payment for a parcel.
     * POST /api/courier/parcels/{id}/pay
     * Auth: SENDER (owner only)
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<?> pay(
            @PathVariable Long id,
            @Valid @RequestBody PayRequest request,
            Authentication authentication
    ) {
        try {
            PaymentResponse response = paymentService.pay(id, request, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * View payment status of a parcel.
     * GET /api/courier/parcels/{id}/payment
     * Auth: SENDER (owner only)
     */
    @GetMapping("/{id}/payment")
    public ResponseEntity<?> getPayment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(
                    paymentService.getPayment(id, authentication.getName())
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
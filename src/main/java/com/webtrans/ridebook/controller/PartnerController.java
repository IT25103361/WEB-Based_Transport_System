package com.seproject.courier.controller;

import com.seproject.courier.dto.partner.AssignmentResponse;
import com.seproject.courier.dto.partner.CreatePartnerRequest;
import com.seproject.courier.dto.partner.LiveLocationRequest;
import com.seproject.courier.dto.partner.LocationUpdateRequest;
import com.seproject.courier.dto.partner.NearbyParcelResponse;
import com.seproject.courier.dto.partner.OnlineStatusRequest;
import com.seproject.courier.dto.partner.PartnerResponse;
import com.seproject.courier.dto.partner.VerifyOtpRequest;
import com.seproject.courier.dto.parcel.CancelRequest;
import com.seproject.courier.dto.parcel.LocationBroadcast;
import com.seproject.courier.service.MatchingService;
import com.seproject.courier.service.PartnerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courier/partner")
public class PartnerController {

    @Autowired private PartnerService partnerService;
    @Autowired private MatchingService matchingService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody CreatePartnerRequest request,
            Authentication authentication
    ) {
        try {
            PartnerResponse response = partnerService.registerAsPartner(
                    request, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        try {
            return ResponseEntity.ok(
                    partnerService.getMyProfile(authentication.getName())
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/online")
    public ResponseEntity<?> setOnline(
            @Valid @RequestBody OnlineStatusRequest request,
            Authentication authentication
    ) {
        try {
            PartnerResponse response = partnerService.setOnlineStatus(
                    authentication.getName(), request.getIsOnline());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/location")
    public ResponseEntity<?> updateLocation(
            @Valid @RequestBody LocationUpdateRequest request,
            Authentication authentication
    ) {
        try {
            PartnerResponse response = partnerService.updateLocation(
                    authentication.getName(), request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Returns the partner's current in-progress job (if any).
     * GET /api/courier/partner/active
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveDelivery(Authentication authentication) {
        try {
            PartnerResponse me = partnerService.getMyProfile(authentication.getName());
            return ResponseEntity.ok(
                    matchingService.getActiveDelivery(me.getId()).orElse(null)
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getNearbyRequests(Authentication authentication) {
        try {
            PartnerResponse me = partnerService.getMyProfile(authentication.getName());
            List<NearbyParcelResponse> list = matchingService.findNearbyParcels(me.getId());
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/accept/{parcelId}")
    public ResponseEntity<?> acceptParcel(
            @PathVariable Long parcelId,
            Authentication authentication
    ) {
        try {
            PartnerResponse me = partnerService.getMyProfile(authentication.getName());
            AssignmentResponse response = matchingService.acceptParcel(me.getId(), parcelId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== STATE TRANSITIONS ====================

    @PostMapping("/parcel/{parcelId}/pickup")
    public ResponseEntity<?> pickup(
            @PathVariable Long parcelId,
            Authentication authentication
    ) {
        try {
            PartnerResponse me = partnerService.getMyProfile(authentication.getName());
            return ResponseEntity.ok(
                    matchingService.updateParcelStatus(me.getId(), parcelId, "pickup")
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/parcel/{parcelId}/in-transit")
    public ResponseEntity<?> inTransit(
            @PathVariable Long parcelId,
            Authentication authentication
    ) {
        try {
            PartnerResponse me = partnerService.getMyProfile(authentication.getName());
            return ResponseEntity.ok(
                    matchingService.updateParcelStatus(me.getId(), parcelId, "in-transit")
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/parcel/{parcelId}/arrived")
    public ResponseEntity<?> arrived(
            @PathVariable Long parcelId,
            Authentication authentication
    ) {
        try {
            PartnerResponse me = partnerService.getMyProfile(authentication.getName());
            return ResponseEntity.ok(
                    matchingService.updateParcelStatus(me.getId(), parcelId, "arrived")
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== VERIFY OTP → DELIVERED ====================

    @PostMapping("/parcel/{parcelId}/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @PathVariable Long parcelId,
            @Valid @RequestBody VerifyOtpRequest request,
            Authentication authentication
    ) {
        try {
            PartnerResponse me = partnerService.getMyProfile(authentication.getName());
            AssignmentResponse response = matchingService.verifyOtpAndDeliver(
                    me.getId(), parcelId, request.getOtp());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== LIVE LOCATION PUSH ====================

    @PostMapping("/parcel/{parcelId}/location")
    public ResponseEntity<?> pushLiveLocation(
            @PathVariable Long parcelId,
            @Valid @RequestBody LiveLocationRequest request,
            Authentication authentication
    ) {
        try {
            PartnerResponse me = partnerService.getMyProfile(authentication.getName());
            LocationBroadcast broadcast = matchingService.pushLiveLocation(
                    me.getId(), parcelId, request.getLat(), request.getLng());
            return ResponseEntity.ok(broadcast);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== PARTNER CANCELS ====================

    /**
     * Partner drops an accepted job with a reason.
     * POST /api/courier/partner/parcel/{parcelId}/cancel
     */
    @PostMapping("/parcel/{parcelId}/cancel")
    public ResponseEntity<?> cancelDelivery(
            @PathVariable Long parcelId,
            @Valid @RequestBody CancelRequest request,
            Authentication authentication
    ) {
        try {
            PartnerResponse me = partnerService.getMyProfile(authentication.getName());
            return ResponseEntity.ok(
                    matchingService.cancelByPartner(me.getId(), parcelId, request)
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== EARNINGS ====================

    /**
     * Earnings summary — total, this month, monthly breakdown, recent jobs.
     * GET /api/courier/partner/earnings
     */
    @GetMapping("/earnings")
    public ResponseEntity<?> getEarnings(Authentication authentication) {
        try {
            PartnerResponse me = partnerService.getMyProfile(authentication.getName());
            return ResponseEntity.ok(matchingService.getEarnings(me.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
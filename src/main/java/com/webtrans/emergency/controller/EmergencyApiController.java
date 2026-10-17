package com.webtrans.emergency.controller;

import com.webtrans.auth.model.User;
import com.webtrans.emergency.dto.EmergencyAdminRequestDto;
import com.webtrans.emergency.dto.EmergencyAdminUpdateDto;
import com.webtrans.emergency.dto.EmergencyCreateRequestDto;
import com.webtrans.emergency.dto.EmergencyDeleteRequestDto;
import com.webtrans.emergency.dto.EmergencyTrackingResponseDto;
import com.webtrans.emergency.dto.EmergencyUpdateRequestDto;
import com.webtrans.emergency.service.EmergencyCsrfService;
import com.webtrans.emergency.service.EmergencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyApiController {

    @Autowired
    private EmergencyService emergencyService;

    @Autowired
    private EmergencyCsrfService csrfService;

    /**
     * Provide or refresh CSRF token for the session.
     */
    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> getCsrfToken(HttpSession session) {
        String token = csrfService.getOrCreateToken(session);
        return ResponseEntity.ok(Map.of("csrfToken", token));
    }

    /**
     * Submit an Emergency Request (Public).
     * Works without login. Rate-limited, CSRF-protected, and idempotent.
     */
    @PostMapping("/request")
    public ResponseEntity<?> submitEmergencyRequest(
            @RequestBody EmergencyCreateRequestDto dto,
            HttpServletRequest request,
            HttpSession session
    ) {
        String clientIp = extractClientIp(request);

        // Allow CSRF token to be sent in header as well as in the body
        if (dto.getCsrfToken() == null || dto.getCsrfToken().isBlank()) {
            String headerToken = request.getHeader("X-CSRF-TOKEN");
            if (headerToken != null && !headerToken.isBlank()) {
                dto.setCsrfToken(headerToken.trim());
            }
        }

        try {
            EmergencyTrackingResponseDto response = emergencyService.createEmergencyRequest(dto, clientIp, session);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "An unexpected error occurred while processing your request. Please call emergency services directly."
            ));
        }
    }

    /**
     * Delete an emergency request with mandatory reason (Requester Action).
     * Protected by cryptographic management token verification, CSRF, and rate limiting.
     */
    @PostMapping(value = {"/request/delete", "/request/{token}/delete"})
    public ResponseEntity<?> deleteEmergencyRequest(
            @PathVariable(required = false) String token,
            @RequestBody com.webtrans.emergency.dto.EmergencyDeleteRequestDto dto,
            HttpServletRequest request,
            HttpSession session
    ) {
        String clientIp = extractClientIp(request);

        if (token != null && !token.isBlank() && (dto.getTrackingToken() == null || dto.getTrackingToken().isBlank())) {
            dto.setTrackingToken(token.trim());
        }

        // Allow CSRF token from header
        if (dto.getCsrfToken() == null || dto.getCsrfToken().isBlank()) {
            String headerToken = request.getHeader("X-CSRF-TOKEN");
            if (headerToken != null && !headerToken.isBlank()) {
                dto.setCsrfToken(headerToken.trim());
            }
        }

        try {
            EmergencyTrackingResponseDto result = emergencyService.deleteEmergencyRequestByRequester(dto, clientIp, session);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Emergency request has been successfully deleted.",
                    "trackingToken", result.getTrackingToken(),
                    "incident", result
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        } catch (java.util.NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "An error occurred while processing the deletion request."
            ));
        }
    }

    /**
     * Update an emergency incident report's location and description (Requester Action).
     */
    @PostMapping(value = {"/request/update", "/request/{token}/update"})
    public ResponseEntity<?> updateEmergencyRequest(
            @PathVariable(required = false) String token,
            @RequestBody EmergencyUpdateRequestDto dto,
            HttpServletRequest request,
            HttpSession session
    ) {
        String clientIp = extractClientIp(request);

        if (token != null && !token.isBlank() && (dto.getTrackingToken() == null || dto.getTrackingToken().isBlank())) {
            dto.setTrackingToken(token.trim());
        }

        // Allow CSRF token from header
        if (dto.getCsrfToken() == null || dto.getCsrfToken().isBlank()) {
            String headerToken = request.getHeader("X-CSRF-TOKEN");
            if (headerToken != null && !headerToken.isBlank()) {
                dto.setCsrfToken(headerToken.trim());
            }
        }

        try {
            EmergencyTrackingResponseDto result = emergencyService.updateEmergencyRequestByRequester(dto, clientIp, session);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Emergency incident details have been successfully updated.",
                    "trackingToken", result.getTrackingToken(),
                    "incident", result
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        } catch (java.util.NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "An error occurred while processing the incident update."
            ));
        }
    }

    /**
     * Public Tracking endpoint by unguessable tracking token.
     */
    @GetMapping("/track/{token}")
    public ResponseEntity<?> trackIncident(@PathVariable String token) {
        Optional<EmergencyTrackingResponseDto> record = emergencyService.getTrackingByToken(token);
        if (record.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "No emergency incident found matching the provided reference token."
            ));
        }
        return ResponseEntity.ok(record.get());
    }

    /**
     * Staff/Admin Console: List emergency requests (active by default, or all including soft-deleted for audit).
     */
    @GetMapping("/admin/requests")
    public ResponseEntity<?> getAllRequestsForAdmin(
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted,
            HttpSession session
    ) {
        if (!isAdminAuthorized(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "error", "Administrator credentials required to access emergency management data."
            ));
        }
        List<EmergencyAdminRequestDto> list = includeDeleted
                ? emergencyService.getAllRequestsForAdmin()
                : emergencyService.getActiveRequests();
        return ResponseEntity.ok(list);
    }

    /**
     * Staff/Admin Console: Update request status and responder allocation.
     */
    @PostMapping("/admin/requests/{id}/status")
    public ResponseEntity<?> updateStatusByAdmin(
            @PathVariable Long id,
            @RequestBody EmergencyAdminUpdateDto updateDto,
            HttpSession session
    ) {
        if (!isAdminAuthorized(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "error", "Administrator credentials required."
            ));
        }
        User adminUser = (User) session.getAttribute("adminUser");
        String adminUsername = adminUser != null ? adminUser.getEmail() : "ADMIN";

        try {
            EmergencyAdminRequestDto updated = emergencyService.updateRequestStatusByAdmin(id, updateDto, adminUsername);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", ex.getMessage()
            ));
        }
    }

    private boolean isAdminAuthorized(HttpSession session) {
        if (session == null) return false;
        Object adminUser = session.getAttribute("adminUser");
        Object adminRole = session.getAttribute("adminRole");
        return adminUser instanceof User && "ADMIN".equals(adminRole);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank() && !"unknown".equalsIgnoreCase(xf)) {
            return xf.split(",")[0].trim();
        }
        String xr = request.getHeader("X-Real-IP");
        if (xr != null && !xr.isBlank() && !"unknown".equalsIgnoreCase(xr)) {
            return xr.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}

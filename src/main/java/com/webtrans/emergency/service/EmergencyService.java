package com.webtrans.emergency.service;

import com.webtrans.emergency.dto.EmergencyAdminRequestDto;
import com.webtrans.emergency.dto.EmergencyAdminUpdateDto;
import com.webtrans.emergency.dto.EmergencyCreateRequestDto;
import com.webtrans.emergency.dto.EmergencyDeleteRequestDto;
import com.webtrans.emergency.dto.EmergencyTrackingResponseDto;
import com.webtrans.emergency.dto.EmergencyUpdateRequestDto;
import com.webtrans.emergency.model.EmergencyRequest;
import com.webtrans.emergency.model.EmergencyServiceType;
import com.webtrans.emergency.model.EmergencyStatus;
import com.webtrans.emergency.model.EmergencyStatusHistory;
import com.webtrans.emergency.repository.EmergencyRequestRepository;
import com.webtrans.emergency.repository.EmergencyStatusHistoryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EmergencyService {

    public static final String HONEST_DEMO_NOTICE = 
            "Your request has been recorded in this project. Emergency responders have not been contacted. " +
            "This is a demonstration system and is not connected to official emergency dispatch.";

    public static final Set<String> ALLOWED_DELETION_REASONS = Set.of(
            "Created by mistake",
            "Duplicate request",
            "Assistance no longer required",
            "Incorrect information",
            "Other"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+?[0-9\\s\\-()]{7,25})$");
    private static final String TOKEN_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // exclude easily confused chars (0,1,I,O)

    @Autowired
    private EmergencyRequestRepository requestRepository;

    @Autowired
    private EmergencyStatusHistoryRepository historyRepository;

    @Autowired
    private EmergencyRateLimiter rateLimiter;

    @Autowired
    private EmergencyCsrfService csrfService;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Public emergency submission.
     * Validates CSRF token, client IP rate limit, idempotency, service selection, agent count, phone, and location.
     * Issues an unguessable tracking token and a separate cryptographically hashed requester management token.
     */
    @Transactional
    public EmergencyTrackingResponseDto createEmergencyRequest(
            EmergencyCreateRequestDto dto,
            String clientIp,
            HttpSession session
    ) {
        // 1. CSRF Token Validation
        if (!csrfService.validateToken(session, dto.getCsrfToken())) {
            throw new IllegalArgumentException("Invalid or expired security token. Please refresh the page and try again.");
        }

        // 2. Rate Limiting Check
        if (!rateLimiter.isAllowed(clientIp)) {
            throw new IllegalStateException("Rate limit exceeded for your network. Please wait a few moments or use direct-call emergency numbers.");
        }

        // 3. Idempotency Check
        String idempotencyKey = (dto.getIdempotencyKey() != null && !dto.getIdempotencyKey().isBlank())
                ? dto.getIdempotencyKey().trim()
                : null;

        if (idempotencyKey != null) {
            Optional<EmergencyRequest> existing = requestRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return mapToTrackingDto(existing.get());
            }
        }

        // 4 & 5. Validate Service Selection and Agent Count via EmergencyServiceSelectionFactory (Simple Factory Pattern)
        if (dto.getServices() == null || dto.getServices().isEmpty()) {
            throw new IllegalArgumentException("Please select at least one emergency service.");
        }

        Set<String> cleanServices = new LinkedHashSet<>();
        List<com.webtrans.emergency.factory.EmergencyServiceSelection> selections = new ArrayList<>();
        Integer requestedAgents = null;

        for (String s : dto.getServices()) {
            if (s == null || s.isBlank()) continue;
            com.webtrans.emergency.factory.EmergencyServiceSelection selection =
                    com.webtrans.emergency.factory.EmergencyServiceSelectionFactory.createSelection(s, dto.getAgentCount());
            selection.validate();
            selections.add(selection);
            cleanServices.add(selection.getServiceCode());
            if (selection.getRequestedAgentCount() != null) {
                requestedAgents = selection.getRequestedAgentCount();
            }
        }

        if (cleanServices.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one emergency service.");
        }

        // 6. Validate Contact & Phone
        if (dto.getCallbackPhone() == null || dto.getCallbackPhone().isBlank()) {
            throw new IllegalArgumentException("A callback phone number is required so emergency contacts can reach you.");
        }
        String cleanPhone = dto.getCallbackPhone().trim();
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            throw new IllegalArgumentException("Please provide a valid phone number (e.g. 0771234567 or +94771234567).");
        }

        // 7. Validate Incident Description
        if (dto.getIncidentDescription() == null || dto.getIncidentDescription().trim().length() < 5) {
            throw new IllegalArgumentException("Please provide a brief description of the incident (at least 5 characters).");
        }
        if (dto.getIncidentDescription().trim().length() > 1000) {
            throw new IllegalArgumentException("Incident description is too long (maximum 1000 characters).");
        }

        // 8. Validate Location
        boolean hasCoords = dto.getLatitude() != null && dto.getLongitude() != null &&
                !Double.isNaN(dto.getLatitude()) && !Double.isNaN(dto.getLongitude());
        boolean hasTextAddress = dto.getLocationAddress() != null && dto.getLocationAddress().trim().length() >= 3;
        boolean hasLandmark = dto.getLocationLandmark() != null && dto.getLocationLandmark().trim().length() >= 3;

        if (!hasCoords && !hasTextAddress && !hasLandmark) {
            throw new IllegalArgumentException("Please set your emergency location on the map or provide a manual address/landmark.");
        }

        if (hasCoords) {
            double lat = dto.getLatitude();
            double lng = dto.getLongitude();
            if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
                throw new IllegalArgumentException("Coordinates provided are invalid.");
            }
        }

        // 9. Generate Unique Unguessable Tracking Token and Separate Management Token
        String trackingToken = generateTrackingToken();
        String rawManagementToken = "MGT-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String managementTokenHash = hashToken(rawManagementToken);

        // 10. Persist Emergency Request
        EmergencyRequest req = EmergencyRequest.builder()
                .trackingToken(trackingToken)
                .idempotencyKey(idempotencyKey)
                .managementTokenHash(managementTokenHash)
                .isDeleted(false)
                .requestedServices(String.join(",", cleanServices))
                .requestedAgentCount(requestedAgents)
                .assignedAgentsCount(0)
                .latitude(hasCoords ? dto.getLatitude() : null)
                .longitude(hasCoords ? dto.getLongitude() : null)
                .locationAddress(dto.getLocationAddress() != null ? dto.getLocationAddress().trim() : null)
                .locationLandmark(dto.getLocationLandmark() != null ? dto.getLocationLandmark().trim() : null)
                .contactName(dto.getContactName() != null && !dto.getContactName().isBlank() ? dto.getContactName().trim() : "Anonymous Citizen")
                .callbackPhone(cleanPhone)
                .incidentDescription(dto.getIncidentDescription().trim())
                .status(EmergencyStatus.SUBMITTED)
                .clientIp(clientIp)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        req = requestRepository.save(req);

        // Add initial status history record explaining honest demo behavior
        req.addStatusHistory(
                EmergencyStatus.SUBMITTED,
                "Request recorded in NEXT GO demonstration system. Responders have NOT been dispatched.",
                "SYSTEM"
        );
        req = requestRepository.save(req);

        EmergencyTrackingResponseDto response = mapToTrackingDto(req);
        // Expose management token ONLY once in the initial creation response
        response.setManagementToken(rawManagementToken);
        response.setCanDelete(true);
        return response;
    }

    /**
     * Public tracking by unguessable tracking token.
     * Returns sanitized, limited details suitable for anonymous requester view.
     * Does NOT expose management tokens or private phone numbers.
     * For soft-deleted requests, returns a minimal "This request is no longer available" response.
     */
    @Transactional(readOnly = true)
    public Optional<EmergencyTrackingResponseDto> getTrackingByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return requestRepository.findByTrackingToken(token.trim().toUpperCase())
                .map(this::mapToTrackingDto);
    }

    /**
     * Requester Soft Deletion with mandatory reason and cryptographic management token verification.
     * Soft deletes the incident, prevents duplicates, preserves audit history, and blocks deletion
     * if responders are already assigned or operational status has advanced.
     */
    @Transactional
    public EmergencyTrackingResponseDto deleteEmergencyRequestByRequester(
            EmergencyDeleteRequestDto dto,
            String clientIp,
            HttpSession session
    ) {
        // 1. CSRF Validation
        if (!csrfService.validateToken(session, dto.getCsrfToken())) {
            throw new IllegalArgumentException("Invalid or expired security token. Please refresh the page and try again.");
        }

        // 2. Rate Limiting Check
        if (!rateLimiter.isAllowed(clientIp)) {
            throw new IllegalStateException("Rate limit exceeded for your network. Please wait a moment before trying again.");
        }

        // 3. Validate Tracking Token & Management Token
        if (dto.getTrackingToken() == null || dto.getTrackingToken().isBlank()) {
            throw new IllegalArgumentException("Emergency tracking reference token is required.");
        }
        if (dto.getManagementToken() == null || dto.getManagementToken().isBlank()) {
            throw new SecurityException("Management authorization token is required to delete this request.");
        }

        // 4. Validate Reason & Note
        if (dto.getReason() == null || dto.getReason().isBlank()) {
            throw new IllegalArgumentException("A deletion reason is mandatory.");
        }

        String selectedReason = dto.getReason().trim();
        String canonicalReason = ALLOWED_DELETION_REASONS.stream()
                .filter(r -> r.equalsIgnoreCase(selectedReason))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid deletion reason. Please select from the allowed list: " + String.join(", ", ALLOWED_DELETION_REASONS)));

        String cleanNote = (dto.getNote() != null && !dto.getNote().isBlank()) ? dto.getNote().trim() : null;

        if ("Other".equalsIgnoreCase(canonicalReason)) {
            if (cleanNote == null || cleanNote.length() < 5) {
                throw new IllegalArgumentException("Please provide an explanation when selecting 'Other' as the deletion reason (at least 5 characters).");
            }
        }
        if (cleanNote != null && cleanNote.length() > 500) {
            throw new IllegalArgumentException("Deletion explanation is too long (maximum 500 characters).");
        }

        // 5. Atomic Lookup & Verification
        EmergencyRequest req = requestRepository.findByTrackingToken(dto.getTrackingToken().trim().toUpperCase())
                .orElseThrow(() -> new NoSuchElementException("No emergency request found matching reference: " + dto.getTrackingToken()));

        // Repeated deletion attempts check
        if (Boolean.TRUE.equals(req.getIsDeleted())) {
            throw new IllegalStateException("This emergency request has already been deleted.");
        }

        // Cryptographic Management Token Verification
        if (req.getManagementTokenHash() == null || req.getManagementTokenHash().isBlank()) {
            throw new SecurityException("This emergency request does not have verified management authorization and cannot be deleted anonymously.");
        }

        String inputHash = hashToken(dto.getManagementToken().trim());
        if (!constantTimeEquals(req.getManagementTokenHash(), inputHash)) {
            throw new SecurityException("Invalid management authorization token. You do not have permission to delete this request.");
        }

        // Status Eligibility Check: only allowed before acceptance / dispatch
        if (req.getStatus() != EmergencyStatus.SUBMITTED && req.getStatus() != EmergencyStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Deletion is unavailable because emergency response agents or units have already been assigned or dispatched to this incident. Please contact emergency services directly.");
        }
        if (req.getAssignedAgentsCount() != null && req.getAssignedAgentsCount() > 0) {
            throw new IllegalStateException("Deletion is unavailable because response agents have already been assigned to this incident.");
        }

        // 6. Perform Soft Deletion
        req.setIsDeleted(true);
        req.setDeletedAt(LocalDateTime.now());
        req.setDeletionReason(canonicalReason);
        req.setDeletionNote(cleanNote);
        req.setDeletedByActorType("REQUESTER");
        req.setStatus(EmergencyStatus.DELETED);
        req.setUpdatedAt(LocalDateTime.now());

        String historyNote = "Incident deleted by requester. Reason: " + canonicalReason + (cleanNote != null ? " (" + cleanNote + ")" : "");
        req.addStatusHistory(EmergencyStatus.DELETED, historyNote, "REQUESTER");

        req = requestRepository.save(req);

        return mapToTrackingDto(req);
    }

    /**
     * Requester Action: Update emergency incident location and description.
     */
    @Transactional
    public EmergencyTrackingResponseDto updateEmergencyRequestByRequester(
            EmergencyUpdateRequestDto dto,
            String clientIp,
            HttpSession session
    ) {
        // 1. CSRF Validation
        if (!csrfService.validateToken(session, dto.getCsrfToken())) {
            throw new IllegalArgumentException("Invalid or expired security token. Please refresh the page and try again.");
        }

        // 2. Rate Limiting Check
        if (!rateLimiter.isAllowed(clientIp)) {
            throw new IllegalStateException("Rate limit exceeded for your network. Please wait a moment before trying again.");
        }

        // 3. Validate Tracking Token
        if (dto.getTrackingToken() == null || dto.getTrackingToken().isBlank()) {
            throw new IllegalArgumentException("Emergency tracking reference token is required.");
        }

        // 4. Validate Location & Description
        if (dto.getLocationAddress() == null || dto.getLocationAddress().trim().isBlank()) {
            throw new IllegalArgumentException("Location address is required.");
        }
        if (dto.getIncidentDescription() == null || dto.getIncidentDescription().trim().length() < 5) {
            throw new IllegalArgumentException("Incident description must be at least 5 characters long.");
        }
        if (dto.getIncidentDescription().trim().length() > 1000) {
            throw new IllegalArgumentException("Incident description cannot exceed 1000 characters.");
        }

        // 5. Lookup & Verify
        EmergencyRequest req = requestRepository.findByTrackingToken(dto.getTrackingToken().trim().toUpperCase())
                .orElseThrow(() -> new NoSuchElementException("No emergency request found matching reference: " + dto.getTrackingToken()));

        if (Boolean.TRUE.equals(req.getIsDeleted())) {
            throw new IllegalStateException("Cannot update a deleted emergency incident.");
        }

        // Verify management token if supplied
        if (dto.getManagementToken() != null && !dto.getManagementToken().isBlank()) {
            if (req.getManagementTokenHash() != null && !req.getManagementTokenHash().isBlank()) {
                String inputHash = hashToken(dto.getManagementToken().trim());
                if (!constantTimeEquals(req.getManagementTokenHash(), inputHash)) {
                    throw new SecurityException("Invalid management authorization token. You do not have permission to update this request.");
                }
            }
        }

        // 6. Update fields
        String updatedAddress = dto.getLocationAddress().trim();
        String updatedLandmark = (dto.getLocationLandmark() != null && !dto.getLocationLandmark().trim().isBlank())
                ? dto.getLocationLandmark().trim()
                : null;
        String updatedDesc = dto.getIncidentDescription().trim();

        req.setLocationAddress(updatedAddress);
        req.setLocationLandmark(updatedLandmark);
        req.setIncidentDescription(updatedDesc);
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            req.setLatitude(dto.getLatitude());
            req.setLongitude(dto.getLongitude());
        }
        req.setUpdatedAt(LocalDateTime.now());

        String historyNote = "Requester updated incident location and description.";
        req.addStatusHistory(req.getStatus(), historyNote, "REQUESTER");

        req = requestRepository.save(req);

        return mapToTrackingDto(req);
    }

    /**
     * Operational console: list active emergency requests (excluding soft-deleted incidents).
     */
    @Transactional(readOnly = true)
    public List<EmergencyAdminRequestDto> getActiveRequests() {
        return requestRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToAdminDto)
                .collect(Collectors.toList());
    }

    /**
     * Operational console: list all emergency requests including soft-deleted ones for audit (authorized staff/admin only).
     */
    @Transactional(readOnly = true)
    public List<EmergencyAdminRequestDto> getAllRequestsForAdmin() {
        return requestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToAdminDto)
                .collect(Collectors.toList());
    }

    /**
     * Operational console: update status or assign agents (authorized staff/admin only).
     */
    @Transactional
    public EmergencyAdminRequestDto updateRequestStatusByAdmin(Long id, EmergencyAdminUpdateDto updateDto, String adminUsername) {
        EmergencyRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Emergency request not found for ID: " + id));

        if (Boolean.TRUE.equals(req.getIsDeleted())) {
            throw new IllegalStateException("Cannot update status of a deleted emergency incident.");
        }

        if (updateDto.getStatus() != null && !updateDto.getStatus().isBlank()) {
            EmergencyStatus newStatus = EmergencyStatus.valueOf(updateDto.getStatus().trim().toUpperCase());
            req.setStatus(newStatus);
            String note = updateDto.getNote() != null && !updateDto.getNote().isBlank()
                    ? updateDto.getNote().trim()
                    : "Status updated to " + newStatus.getDisplayName();
            req.addStatusHistory(newStatus, note, adminUsername != null ? adminUsername : "ADMIN");
        }

        if (updateDto.getAssignedAgents() != null) {
            int agents = Math.max(0, updateDto.getAssignedAgents());
            req.setAssignedAgentsCount(agents);
        }

        req.setUpdatedAt(LocalDateTime.now());
        req = requestRepository.save(req);
        return mapToAdminDto(req);
    }

    private EmergencyTrackingResponseDto mapToTrackingDto(EmergencyRequest req) {
        boolean isDel = Boolean.TRUE.equals(req.getIsDeleted());

        if (isDel) {
            return EmergencyTrackingResponseDto.builder()
                    .trackingToken(req.getTrackingToken())
                    .status("DELETED")
                    .statusDisplayName("Deleted")
                    .systemNotice("This request is no longer available.")
                    .demonstrationMode(true)
                    .isDeleted(true)
                    .canDelete(false)
                    .managementToken(null)
                    .requestedServices(Collections.emptyList())
                    .locationSummary(null)
                    .incidentDescription(null)
                    .contactName(null)
                    .createdAt(req.getCreatedAt())
                    .updatedAt(req.getUpdatedAt())
                    .history(Collections.emptyList())
                    .build();
        }

        List<String> services = Arrays.stream(req.getRequestedServices().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        String locationSummary;
        if (req.getLocationAddress() != null && !req.getLocationAddress().isBlank()) {
            locationSummary = req.getLocationAddress();
            if (req.getLocationLandmark() != null && !req.getLocationLandmark().isBlank()) {
                locationSummary += " (Near: " + req.getLocationLandmark() + ")";
            }
        } else if (req.getLatitude() != null && req.getLongitude() != null) {
            locationSummary = String.format("Lat: %.5f, Lng: %.5f", req.getLatitude(), req.getLongitude());
            if (req.getLocationLandmark() != null && !req.getLocationLandmark().isBlank()) {
                locationSummary += " (Near: " + req.getLocationLandmark() + ")";
            }
        } else {
            locationSummary = req.getLocationLandmark() != null ? req.getLocationLandmark() : "Location details recorded";
        }

        List<EmergencyTrackingResponseDto.StatusHistoryItemDto> historyList = Collections.emptyList();
        if (req.getStatusHistory() != null) {
            historyList = req.getStatusHistory().stream()
                    .map(h -> EmergencyTrackingResponseDto.StatusHistoryItemDto.builder()
                            .status(h.getStatus().name())
                            .note(h.getNote())
                            .timestamp(h.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
        }

        boolean canDelete = (req.getStatus() == EmergencyStatus.SUBMITTED || req.getStatus() == EmergencyStatus.UNDER_REVIEW)
                && (req.getAssignedAgentsCount() == null || req.getAssignedAgentsCount() == 0);

        return EmergencyTrackingResponseDto.builder()
                .trackingToken(req.getTrackingToken())
                .requestedServices(services)
                .requestedAgentCount(req.getRequestedAgentCount())
                .assignedAgentsCount(req.getAssignedAgentsCount())
                .status(req.getStatus().name())
                .statusDisplayName(req.getStatus().getDisplayName())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .locationSummary(locationSummary)
                .locationAddress(req.getLocationAddress())
                .locationLandmark(req.getLocationLandmark())
                .incidentDescription(req.getIncidentDescription())
                .contactName(req.getContactName())
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .systemNotice(HONEST_DEMO_NOTICE)
                .demonstrationMode(true)
                .isDeleted(false)
                .canDelete(canDelete)
                .managementToken(null) // Never expose management token in tracking
                .history(historyList)
                .build();
    }

    private EmergencyAdminRequestDto mapToAdminDto(EmergencyRequest req) {
        List<String> services = Arrays.stream(req.getRequestedServices().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        List<EmergencyTrackingResponseDto.StatusHistoryItemDto> historyList = Collections.emptyList();
        if (req.getStatusHistory() != null) {
            historyList = req.getStatusHistory().stream()
                    .map(h -> EmergencyTrackingResponseDto.StatusHistoryItemDto.builder()
                            .status(h.getStatus().name())
                            .note(h.getNote())
                            .timestamp(h.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
        }

        return EmergencyAdminRequestDto.builder()
                .id(req.getId())
                .trackingToken(req.getTrackingToken())
                .requestedServices(services)
                .requestedAgentCount(req.getRequestedAgentCount())
                .assignedAgentsCount(req.getAssignedAgentsCount())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .locationAddress(req.getLocationAddress())
                .locationLandmark(req.getLocationLandmark())
                .contactName(req.getContactName())
                .callbackPhone(req.getCallbackPhone())
                .incidentDescription(req.getIncidentDescription())
                .status(req.getStatus().name())
                .clientIp(req.getClientIp())
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .isDeleted(Boolean.TRUE.equals(req.getIsDeleted()))
                .deletedAt(req.getDeletedAt())
                .deletionReason(req.getDeletionReason())
                .deletionNote(req.getDeletionNote())
                .deletedByActorType(req.getDeletedByActorType())
                .history(historyList)
                .build();
    }

    private String generateTrackingToken() {
        for (int attempts = 0; attempts < 10; attempts++) {
            StringBuilder sb = new StringBuilder("EMG-");
            for (int i = 0; i < 8; i++) {
                int idx = secureRandom.nextInt(TOKEN_ALPHABET.length());
                sb.append(TOKEN_ALPHABET.charAt(idx));
            }
            String candidate = sb.toString();
            if (requestRepository.findByTrackingToken(candidate).isEmpty()) {
                return candidate;
            }
        }
        // Fallback with UUID substring
        return "EMG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

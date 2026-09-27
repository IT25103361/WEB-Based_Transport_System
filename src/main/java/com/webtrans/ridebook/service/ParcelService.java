package com.seproject.courier.service;

import com.seproject.courier.dto.parcel.CancelRequest;
import com.seproject.courier.dto.parcel.CreateParcelRequest;
import com.seproject.courier.dto.parcel.ParcelResponse;
import com.seproject.courier.dto.parcel.StatusHistoryResponse;
import com.seproject.courier.entity.Otp;
import com.seproject.courier.entity.Parcel;
import com.seproject.courier.entity.StatusHistory;
import com.seproject.courier.entity.User;
import com.seproject.courier.repository.AssignmentRepository;
import com.seproject.courier.repository.OtpRepository;
import com.seproject.courier.repository.ParcelRepository;
import com.seproject.courier.repository.StatusHistoryRepository;
import com.seproject.courier.repository.UserRepository;
import com.seproject.courier.util.PricingCalculator;
import com.seproject.courier.util.TrackingCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParcelService {

    @Autowired private ParcelRepository parcelRepository;
    @Autowired private StatusHistoryRepository statusHistoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private OtpRepository otpRepository;
    @Autowired private TrackingCodeGenerator trackingCodeGenerator;
    @Autowired private PricingCalculator pricingCalculator;

    // ==================== CREATE ====================
    @Transactional
    public ParcelResponse createParcel(CreateParcelRequest request, String senderEmail) {

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found: " + senderEmail));

        Parcel parcel = Parcel.builder()
                .trackingCode(generateUniqueTrackingCode())
                .sender(sender)
                .receiverName(request.getReceiverName())
                .receiverEmail(request.getReceiverEmail())
                .receiverPhone(request.getReceiverPhone())
                .pickupAddress(orCoords(request.getPickupAddress(), request.getPickupLat(), request.getPickupLng()))
                .pickupLat(request.getPickupLat())
                .pickupLng(request.getPickupLng())
                .dropAddress(orCoords(request.getDropAddress(), request.getDropLat(), request.getDropLng()))
                .dropLat(request.getDropLat())
                .dropLng(request.getDropLng())
                .weightKg(request.getWeightKg())
                .size(request.getSize())
                .serviceTier(request.getServiceTier())
                .scheduledAt(request.getScheduledAt())
                .status(Parcel.Status.REQUESTED)
                .build();

        parcel.setPrice(pricingCalculator.calculate(parcel));

        Parcel saved = parcelRepository.save(parcel);

        statusHistoryRepository.save(StatusHistory.builder()
                .parcel(saved)
                .status(Parcel.Status.REQUESTED)
                .note("Parcel created by sender")
                .changedBy(sender)
                .lat(saved.getPickupLat())
                .lng(saved.getPickupLng())
                .build());

        return toResponse(saved);
    }

    // ==================== READ ====================
    public List<ParcelResponse> getMyParcels(String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        return parcelRepository.findBySenderIdOrderByCreatedAtDesc(sender.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ParcelResponse getByTrackingCode(String trackingCode) {
        Parcel parcel = parcelRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new RuntimeException("Parcel not found: " + trackingCode));
        return toResponse(parcel);
    }

    public ParcelResponse getById(Long id, String senderEmail) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parcel not found: " + id));

        if (!parcel.getSender().getEmail().equals(senderEmail)) {
            throw new RuntimeException("Not authorized to view this parcel");
        }
        return toResponse(parcel);
    }

    public List<StatusHistoryResponse> getTimeline(Long parcelId, String senderEmail) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new RuntimeException("Parcel not found: " + parcelId));

        if (!parcel.getSender().getEmail().equals(senderEmail)) {
            throw new RuntimeException("Not authorized to view this parcel");
        }

        return statusHistoryRepository.findByParcelIdOrderByChangedAtAsc(parcelId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    // ==================== CANCEL (SENDER) ====================
    @Transactional
    public ParcelResponse cancelParcel(Long parcelId, CancelRequest request, String senderEmail) {

        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new RuntimeException("Parcel not found: " + parcelId));

        if (!parcel.getSender().getEmail().equals(senderEmail)) {
            throw new RuntimeException("Not authorized to cancel this parcel");
        }

        if (parcel.getStatus() != Parcel.Status.REQUESTED
                && parcel.getStatus() != Parcel.Status.MATCHED
                && parcel.getStatus() != Parcel.Status.ACCEPTED) {
            throw new RuntimeException("Too late to cancel — parcel is already " + parcel.getStatus());
        }

        String reason = buildReason(request);

        parcel.setStatus(Parcel.Status.CANCELLED);
        parcel.setCancelReason(reason);
        parcel.setCancelledBy("SENDER");
        parcel.setCancelledAt(LocalDateTime.now());
        parcelRepository.save(parcel);

        assignmentRepository.findByParcelId(parcelId).ifPresent(a -> {
            a.getPartner().setIsAvailable(true);
            assignmentRepository.delete(a);
        });

        statusHistoryRepository.save(StatusHistory.builder()
                .parcel(parcel)
                .status(Parcel.Status.CANCELLED)
                .note("Cancelled by sender — " + reason)
                .changedBy(parcel.getSender())
                .build());

        return toResponse(parcel);
    }

    // ==================== HELPERS ====================

    public static String buildReason(CancelRequest request) {
        StringBuilder sb = new StringBuilder();
        if (request != null && request.getReasons() != null && !request.getReasons().isEmpty()) {
            sb.append(String.join(", ", request.getReasons()));
        }
        if (request != null && request.getNote() != null && !request.getNote().isBlank()) {
            if (sb.length() > 0) sb.append(" — ");
            sb.append(request.getNote().trim());
        }
        return sb.length() == 0 ? "No reason given" : sb.toString();
    }

    private String orCoords(String address, BigDecimal lat, BigDecimal lng) {
        if (address != null && !address.isBlank()) return address.trim();
        return "Pinned location (" +
                lat.setScale(5, RoundingMode.HALF_UP) + ", " +
                lng.setScale(5, RoundingMode.HALF_UP) + ")";
    }

    private String generateUniqueTrackingCode() {
        for (int i = 0; i < 5; i++) {
            String code = trackingCodeGenerator.generate();
            if (parcelRepository.findByTrackingCode(code).isEmpty()) {
                return code;
            }
        }
        throw new RuntimeException("Failed to generate unique tracking code");
    }

    private ParcelResponse toResponse(Parcel p) {
        return ParcelResponse.builder()
                .id(p.getId())
                .trackingCode(p.getTrackingCode())
                .senderId(p.getSender().getId())
                .senderName(p.getSender().getName())
                .receiverName(p.getReceiverName())
                .receiverEmail(p.getReceiverEmail())
                .receiverPhone(p.getReceiverPhone())
                .pickupAddress(p.getPickupAddress())
                .pickupLat(p.getPickupLat())
                .pickupLng(p.getPickupLng())
                .dropAddress(p.getDropAddress())
                .dropLat(p.getDropLat())
                .dropLng(p.getDropLng())
                .weightKg(p.getWeightKg())
                .size(p.getSize())
                .serviceTier(p.getServiceTier())
                .status(p.getStatus())
                .price(p.getPrice())
                .cancelReason(p.getCancelReason())
                .cancelledBy(p.getCancelledBy())
                .refundEligible(p.getStatus() == Parcel.Status.FAILED && "PARTNER".equals(p.getCancelledBy()))
                .deliveryOtp(resolveActiveOtp(p))
                .scheduledAt(p.getScheduledAt())
                .createdAt(p.getCreatedAt())
                .build();
    }

    /**
     * Reveals the OTP only while the parcel is ARRIVED and the code hasn't been
     * used or expired. Otherwise returns null so the sender's UI stays clean.
     */
    private String resolveActiveOtp(Parcel p) {
        if (p.getStatus() != Parcel.Status.ARRIVED) return null;
        Otp otp = otpRepository.findByParcelId(p.getId()).orElse(null);
        if (otp == null) return null;
        if (Boolean.TRUE.equals(otp.getVerified())) return null;
        if (otp.getExpiresAt() == null || otp.getExpiresAt().isBefore(LocalDateTime.now())) return null;
        return otp.getOtpCode();
    }

    private LocalDateTime resolveActiveOtpExpiry(Parcel p) {
        if (p.getStatus() != Parcel.Status.ARRIVED) return null;
        Otp otp = otpRepository.findByParcelId(p.getId()).orElse(null);
        if (otp == null) return null;
        if (Boolean.TRUE.equals(otp.getVerified())) return null;
        if (otp.getExpiresAt() == null || otp.getExpiresAt().isBefore(LocalDateTime.now())) return null;
        return otp.getExpiresAt();
    }

    private StatusHistoryResponse toHistoryResponse(StatusHistory h) {
        return StatusHistoryResponse.builder()
                .id(h.getId())
                .status(h.getStatus())
                .note(h.getNote())
                .lat(h.getLat())
                .lng(h.getLng())
                .changedByName(h.getChangedBy() != null ? h.getChangedBy().getName() : "System")
                .changedAt(h.getChangedAt())
                .build();
    }
}
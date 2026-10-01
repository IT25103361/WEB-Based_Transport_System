package com.webtrans.Courier.service;

import com.webtrans.Courier.dto.parcel.StatusHistoryResponse;
import com.webtrans.Courier.dto.parcel.TrackingResponse;
import com.webtrans.Courier.model.Assignment;
import com.webtrans.Courier.model.Otp;
import com.webtrans.Courier.model.Parcel;
import com.webtrans.Courier.model.Partner;
import com.webtrans.Courier.repository.AssignmentRepository;
import com.webtrans.Courier.repository.OtpRepository;
import com.webtrans.Courier.repository.ParcelRepository;
import com.webtrans.Courier.repository.StatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TrackingService {

    @Autowired private ParcelRepository parcelRepository;
    @Autowired private StatusHistoryRepository statusHistoryRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private OtpRepository otpRepository;

    public TrackingResponse track(String trackingCode) {

        Parcel parcel = parcelRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new RuntimeException("No parcel found for tracking code: " + trackingCode));

        List<StatusHistoryResponse> timeline = statusHistoryRepository
                .findByParcelIdOrderByChangedAtAsc(parcel.getId())
                .stream()
                .map(h -> StatusHistoryResponse.builder()
                        .id(h.getId())
                        .status(h.getStatus())
                        .note(h.getNote())
                        .lat(h.getLat())
                        .lng(h.getLng())
                        .changedByName(h.getChangedBy() != null ? h.getChangedBy().getName() : "System")
                        .changedAt(h.getChangedAt())
                        .build())
                .toList();

        // The assigned partner (if any) — used for the live marker on the map.
        Optional<Assignment> assignment = assignmentRepository.findByParcelId(parcel.getId());
        Partner partner = assignment.map(Assignment::getPartner).orElse(null);

        // The OTP is only revealed once the partner has physically arrived, so it can't
        // be harvested earlier by anyone who happens to know the tracking code.
        String otpCode = null;
        LocalDateTime otpExpiry = null;
        if (parcel.getStatus() == Parcel.Status.ARRIVED) {
            Otp otp = otpRepository.findByParcelId(parcel.getId()).orElse(null);
            if (otp != null
                    && !Boolean.TRUE.equals(otp.getVerified())
                    && otp.getExpiresAt().isAfter(LocalDateTime.now())) {
                otpCode = otp.getOtpCode();
                otpExpiry = otp.getExpiresAt();
            }
        }

        return TrackingResponse.builder()
                .parcelId(parcel.getId())
                .trackingCode(parcel.getTrackingCode())
                .status(parcel.getStatus())
                .statusMessage(friendlyMessage(parcel.getStatus()))
                .pickupAddress(parcel.getPickupAddress())
                .pickupLat(parcel.getPickupLat())
                .pickupLng(parcel.getPickupLng())
                .dropAddress(parcel.getDropAddress())
                .dropLat(parcel.getDropLat())
                .dropLng(parcel.getDropLng())
                .receiverFirstName(firstName(parcel.getReceiverName()))
                .serviceTier(parcel.getServiceTier())
                .price(parcel.getPrice())
                .createdAt(parcel.getCreatedAt())
                .expectedDelivery(estimateDelivery(parcel))
                .partnerId(partner != null ? partner.getId() : null)
                .partnerName(partner != null ? partner.getUser().getName() : null)
                .partnerVehicleType(partner != null ? partner.getVehicleType() : null)
                .partnerLat(partner != null ? partner.getCurrentLat() : null)
                .partnerLng(partner != null ? partner.getCurrentLng() : null)
                .cancelReason(parcel.getCancelReason())
                .cancelledBy(parcel.getCancelledBy())
                .deliveryOtp(otpCode)
                .otpExpiresAt(otpExpiry)
                .timeline(timeline)
                .build();
    }

    // ==================== HELPERS ====================
    private String friendlyMessage(Parcel.Status status) {
        return switch (status) {
            case REQUESTED -> "Parcel created — waiting for a delivery partner to accept";
            case MATCHED -> "A delivery partner has accepted your parcel";
            case ACCEPTED -> "Partner is on the way to pick up your parcel";
            case PICKED_UP -> "Parcel picked up from sender";
            case IN_TRANSIT -> "On the way to the receiver";
            case ARRIVED -> "Partner has arrived — share your delivery code to collect the parcel";
            case DELIVERED -> "Delivered successfully";
            case CANCELLED -> "Cancelled";
            case FAILED -> "Delivery failed";
            case RETURNED -> "Returned to sender";
        };
    }

    private String firstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        return fullName.trim().split("\\s+")[0];
    }

    private LocalDateTime estimateDelivery(Parcel parcel) {
        if (parcel.getCreatedAt() == null) return null;

        if (parcel.getServiceTier() == Parcel.ServiceTier.INSTANT) {
            return parcel.getCreatedAt().plusHours(2);   // instant: 2 hours
        }
        return parcel.getCreatedAt().plusDays(2);        // islandwide: 2 days
    }
}
package com.webtrans.Courier.service;


import com.webtrans.Courier.dto.parcel.CancelRequest;
import com.webtrans.Courier.dto.parcel.LocationBroadcast;
import com.webtrans.Courier.dto.partner.ActiveDeliveryResponse;
import com.webtrans.Courier.dto.partner.AssignmentResponse;
import com.webtrans.Courier.dto.partner.EarningsResponse;
import com.webtrans.Courier.dto.partner.NearbyParcelResponse;
import com.webtrans.Courier.model.Assignment;
import com.webtrans.Courier.model.Parcel;
import com.webtrans.Courier.model.Partner;
import com.webtrans.Courier.model.StatusHistory;
import com.webtrans.Courier.repository.AssignmentRepository;
import com.webtrans.Courier.repository.ParcelRepository;
import com.webtrans.Courier.repository.PartnerRepository;
import com.webtrans.Courier.repository.StatusHistoryRepository;
import com.webtrans.Courier.service.observer.ParcelStatusEvent;
import com.webtrans.Courier.service.observer.ParcelStatusPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    /** Maximum distance (km) for a partner to see an INSTANT parcel. */
    private static final double INSTANT_RADIUS_KM = 10.0;

    @Autowired private PartnerRepository partnerRepository;
    @Autowired private ParcelStatusPublisher parcelStatusPublisher;
    @Autowired private ParcelRepository parcelRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private StatusHistoryRepository statusHistoryRepository;
    @Autowired private OtpService otpService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    // ==================== NEARBY MATCHING ====================
    public List<NearbyParcelResponse> findNearbyParcels(Long partnerId) {

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found: " + partnerId));

        if (!Boolean.TRUE.equals(partner.getIsOnline()) ||
                !Boolean.TRUE.equals(partner.getIsAvailable())) {
            throw new RuntimeException("You must be online and available to see requests");
        }

        if (partner.getCurrentLat() == null || partner.getCurrentLng() == null) {
            throw new RuntimeException("Set your location first (POST /api/courier/partner/location)");
        }

        double partnerLat = partner.getCurrentLat().doubleValue();
        double partnerLng = partner.getCurrentLng().doubleValue();

        List<Parcel> pendingParcels = parcelRepository.findByStatus(Parcel.Status.REQUESTED);

        return pendingParcels.stream()
                .map(p -> {
                    double distance = haversineKm(
                            partnerLat, partnerLng,
                            p.getPickupLat().doubleValue(),
                            p.getPickupLng().doubleValue()
                    );
                    return Map.entry(p, distance);
                })
                .filter(entry -> {
                    Parcel p = entry.getKey();
                    if (p.getServiceTier() == Parcel.ServiceTier.ISLANDWIDE) return true;
                    return entry.getValue() <= INSTANT_RADIUS_KM;
                })
                .sorted(Comparator.comparingDouble(entry -> entry.getValue()))
                .map(entry -> toResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    // ==================== ACCEPT PARCEL (FIRST-ACCEPT-WINS) ====================
    @Transactional
    public AssignmentResponse acceptParcel(Long partnerId, Long parcelId) {

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        if (!Boolean.TRUE.equals(partner.getIsOnline()) ||
                !Boolean.TRUE.equals(partner.getIsAvailable())) {
            throw new RuntimeException("You must be online and available to accept parcels");
        }

        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new RuntimeException("Parcel not found: " + parcelId));

        if (parcel.getStatus() != Parcel.Status.REQUESTED) {
            throw new RuntimeException("Parcel is no longer available (status: " + parcel.getStatus() + ")");
        }

        if (assignmentRepository.existsByParcelId(parcelId)) {
            throw new RuntimeException("Parcel has already been accepted by another partner");
        }

        Assignment assignment = Assignment.builder()
                .parcel(parcel)
                .partner(partner)
                .acceptedAt(LocalDateTime.now())
                .build();

        try {
            assignment = assignmentRepository.saveAndFlush(assignment);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException("Another partner just accepted this parcel. Try another.");
        }

        parcel.setStatus(Parcel.Status.MATCHED);
        parcelRepository.save(parcel);

        statusHistoryRepository.save(StatusHistory.builder()
                .parcel(parcel)
                .status(Parcel.Status.MATCHED)
                .note("Accepted by partner " + partner.getUser().getName())
                .changedBy(partner.getUser())
                .lat(partner.getCurrentLat())
                .lng(partner.getCurrentLng())
                .build());

        partner.setIsAvailable(false);
        partnerRepository.save(partner);

        return buildAssignmentResponse(assignment);
    }

    // ==================== CURRENT ACTIVE DELIVERY ====================
    private static final Set<Parcel.Status> ACTIVE_STATUSES = Set.of(
            Parcel.Status.MATCHED, Parcel.Status.ACCEPTED, Parcel.Status.PICKED_UP,
            Parcel.Status.IN_TRANSIT, Parcel.Status.ARRIVED
    );

    public Optional<ActiveDeliveryResponse> getActiveDelivery(Long partnerId) {
        return assignmentRepository.findByPartnerId(partnerId).stream()
                .filter(a -> ACTIVE_STATUSES.contains(a.getParcel().getStatus()))
                .max(Comparator.comparing(Assignment::getAssignedAt))
                .map(a -> {
                    Parcel p = a.getParcel();
                    return ActiveDeliveryResponse.builder()
                            .assignmentId(a.getId())
                            .parcelId(p.getId())
                            .trackingCode(p.getTrackingCode())
                            .status(p.getStatus())
                            .senderName(p.getSender().getName())
                            .receiverName(p.getReceiverName())
                            .receiverPhone(p.getReceiverPhone())
                            .pickupAddress(p.getPickupAddress())
                            .pickupLat(p.getPickupLat())
                            .pickupLng(p.getPickupLng())
                            .dropAddress(p.getDropAddress())
                            .dropLat(p.getDropLat())
                            .dropLng(p.getDropLng())
                            .price(p.getPrice())
                            .otpRequired(p.getStatus() == Parcel.Status.ARRIVED)
                            .build();
                });
    }

    // ==================== STATE TRANSITIONS ====================
    @Transactional
    public AssignmentResponse updateParcelStatus(Long partnerId, Long parcelId, String action) {

        Assignment assignment = assignmentRepository.findByParcelId(parcelId)
                .orElseThrow(() -> new RuntimeException("No assignment exists for this parcel"));

        if (!assignment.getPartner().getId().equals(partnerId)) {
            throw new RuntimeException("You are not the assigned partner for this parcel");
        }

        Parcel parcel = assignment.getParcel();
        Parcel.Status current = parcel.getStatus();
        Parcel.Status next;

        switch (action) {
            case "pickup" -> {
                if (current != Parcel.Status.MATCHED) {
                    throw new RuntimeException("Cannot pickup — current status is " + current);
                }
                next = Parcel.Status.PICKED_UP;
                assignment.setPickedUpAt(LocalDateTime.now());
            }
            case "in-transit" -> {
                if (current != Parcel.Status.PICKED_UP) {
                    throw new RuntimeException("Cannot mark in-transit — current status is " + current);
                }
                next = Parcel.Status.IN_TRANSIT;
            }
            case "arrived" -> {
                if (current != Parcel.Status.IN_TRANSIT) {
                    throw new RuntimeException("Cannot mark arrived — current status is " + current);
                }
                next = Parcel.Status.ARRIVED;
            }
            default -> throw new RuntimeException("Unknown action: " + action);
        }

        parcel.setStatus(next);
        parcelRepository.save(parcel);

        // ---- Publish ONE event — observers handle the rest ----
        parcelStatusPublisher.publish(ParcelStatusEvent.builder()
                .parcel(parcel)
                .oldStatus(current)
                .newStatus(next)
                .changedBy(assignment.getPartner().getUser())
                .note(actionNote(action))
                .lat(assignment.getPartner().getCurrentLat())
                .lng(assignment.getPartner().getCurrentLng())
                .build());

        assignmentRepository.save(assignment);

        return buildAssignmentResponse(assignment);
    }
    // ==================== VERIFY OTP → DELIVERED ====================
    @Transactional
    public AssignmentResponse verifyOtpAndDeliver(Long partnerId, Long parcelId, String otpCode) {

        Assignment assignment = assignmentRepository.findByParcelId(parcelId)
                .orElseThrow(() -> new RuntimeException("No assignment exists for this parcel"));

        if (!assignment.getPartner().getId().equals(partnerId)) {
            throw new RuntimeException("You are not the assigned partner for this parcel");
        }

        Parcel parcel = assignment.getParcel();

        if (parcel.getStatus() != Parcel.Status.ARRIVED) {
            throw new RuntimeException("Cannot deliver — parcel status is " + parcel.getStatus());
        }

        otpService.verify(parcel, otpCode);

        parcel.setStatus(Parcel.Status.DELIVERED);
        parcelRepository.save(parcel);

        assignment.setDeliveredAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        statusHistoryRepository.save(StatusHistory.builder()
                .parcel(parcel)
                .status(Parcel.Status.DELIVERED)
                .note("Delivered — OTP verified")
                .changedBy(assignment.getPartner().getUser())
                .build());

        Partner partner = assignment.getPartner();
        partner.setIsAvailable(true);
        partnerRepository.save(partner);

        return buildAssignmentResponse(assignment);
    }

    // ==================== PARTNER CANCELS / RELEASES A JOB ====================
    @Transactional
    public Map<String, Object> cancelByPartner(Long partnerId, Long parcelId, CancelRequest request) {

        Assignment assignment = assignmentRepository.findByParcelId(parcelId)
                .orElseThrow(() -> new RuntimeException("No assignment exists for this parcel"));

        if (!assignment.getPartner().getId().equals(partnerId)) {
            throw new RuntimeException("You are not the assigned partner for this parcel");
        }

        Parcel parcel = assignment.getParcel();

        if (parcel.getStatus() == Parcel.Status.DELIVERED
                || parcel.getStatus() == Parcel.Status.CANCELLED) {
            throw new RuntimeException("This delivery is already " + parcel.getStatus());
        }

        String reason = ParcelService.buildReason(request);
        Partner partner = assignment.getPartner();

        boolean alreadyCarrying = parcel.getStatus() == Parcel.Status.PICKED_UP
                || parcel.getStatus() == Parcel.Status.IN_TRANSIT
                || parcel.getStatus() == Parcel.Status.ARRIVED;

        Parcel.Status next = alreadyCarrying ? Parcel.Status.FAILED : Parcel.Status.REQUESTED;

        parcel.setStatus(next);
        parcel.setCancelReason(reason);
        parcel.setCancelledBy("PARTNER");
        parcel.setCancelledAt(LocalDateTime.now());
        parcelRepository.save(parcel);

        statusHistoryRepository.save(StatusHistory.builder()
                .parcel(parcel)
                .status(next)
                .note("Cancelled by partner " + partner.getUser().getName() + " — " + reason)
                .changedBy(partner.getUser())
                .lat(partner.getCurrentLat())
                .lng(partner.getCurrentLng())
                .build());

        assignmentRepository.delete(assignment);

        partner.setIsAvailable(true);
        partnerRepository.save(partner);

        return Map.of(
                "parcelId", parcel.getId(),
                "trackingCode", parcel.getTrackingCode(),
                "status", parcel.getStatus().name(),
                "reason", reason
        );
    }

    // ==================== EARNINGS ====================
    public EarningsResponse getEarnings(Long partnerId) {

        List<Assignment> delivered = assignmentRepository.findByPartnerId(partnerId).stream()
                .filter(a -> a.getDeliveredAt() != null
                        && a.getParcel().getStatus() == Parcel.Status.DELIVERED)
                .sorted(Comparator.comparing(Assignment::getDeliveredAt).reversed())
                .toList();

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal thisMonth = BigDecimal.ZERO;
        int thisMonthCount = 0;

        YearMonth now = YearMonth.now();
        Map<YearMonth, BigDecimal> monthAmount = new LinkedHashMap<>();
        Map<YearMonth, Integer> monthCount = new LinkedHashMap<>();

        for (Assignment a : delivered) {
            BigDecimal price = a.getParcel().getPrice() == null
                    ? BigDecimal.ZERO : a.getParcel().getPrice();
            total = total.add(price);

            YearMonth ym = YearMonth.from(a.getDeliveredAt());
            monthAmount.merge(ym, price, BigDecimal::add);
            monthCount.merge(ym, 1, Integer::sum);

            if (ym.equals(now)) {
                thisMonth = thisMonth.add(price);
                thisMonthCount++;
            }
        }

        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMM yyyy");
        List<EarningsResponse.MonthlyBucket> monthly = new ArrayList<>();
        monthAmount.forEach((ym, amt) -> monthly.add(
                EarningsResponse.MonthlyBucket.builder()
                        .month(ym.toString())
                        .label(ym.atDay(1).format(labelFmt))
                        .amount(amt.setScale(2, RoundingMode.HALF_UP))
                        .deliveries(monthCount.get(ym))
                        .build()
        ));
        monthly.sort(Comparator.comparing(EarningsResponse.MonthlyBucket::getMonth).reversed());

        List<EarningsResponse.DeliveryLine> recent = delivered.stream()
                .limit(20)
                .map(a -> EarningsResponse.DeliveryLine.builder()
                        .parcelId(a.getParcel().getId())
                        .trackingCode(a.getParcel().getTrackingCode())
                        .dropAddress(a.getParcel().getDropAddress())
                        .amount(a.getParcel().getPrice())
                        .deliveredAt(a.getDeliveredAt())
                        .build())
                .toList();

        return EarningsResponse.builder()
                .totalEarned(total.setScale(2, RoundingMode.HALF_UP))
                .thisMonthEarned(thisMonth.setScale(2, RoundingMode.HALF_UP))
                .totalDeliveries(delivered.size())
                .thisMonthDeliveries(thisMonthCount)
                .monthly(monthly)
                .recent(recent)
                .build();
    }

    // ==================== LIVE LOCATION PUSH ====================
    @Transactional
    public LocationBroadcast pushLiveLocation(Long partnerId, Long parcelId,
                                              BigDecimal lat, BigDecimal lng) {

        Assignment assignment = assignmentRepository.findByParcelId(parcelId)
                .orElseThrow(() -> new RuntimeException("No assignment exists for this parcel"));

        if (!assignment.getPartner().getId().equals(partnerId)) {
            throw new RuntimeException("You are not the assigned partner for this parcel");
        }

        Partner partner = assignment.getPartner();
        Parcel parcel = assignment.getParcel();

        partner.setCurrentLat(lat);
        partner.setCurrentLng(lng);
        partnerRepository.save(partner);

        LocationBroadcast payload = LocationBroadcast.builder()
                .parcelId(parcel.getId())
                .trackingCode(parcel.getTrackingCode())
                .partnerId(partner.getId())
                .partnerName(partner.getUser().getName())
                .lat(lat)
                .lng(lng)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/parcel/" + parcel.getId() + "/location",
                payload
        );

        return payload;
    }

    // ==================== HELPERS ====================
    private AssignmentResponse buildAssignmentResponse(Assignment a) {
        return AssignmentResponse.builder()
                .assignmentId(a.getId())
                .parcelId(a.getParcel().getId())
                .trackingCode(a.getParcel().getTrackingCode())
                .parcelStatus(a.getParcel().getStatus())
                .partnerId(a.getPartner().getId())
                .partnerName(a.getPartner().getUser().getName())
                .assignedAt(a.getAssignedAt())
                .acceptedAt(a.getAcceptedAt())
                .build();
    }

    private NearbyParcelResponse toResponse(Parcel p, double distanceKm) {
        return NearbyParcelResponse.builder()
                .parcelId(p.getId())
                .trackingCode(p.getTrackingCode())
                .senderName(p.getSender().getName())
                .pickupAddress(p.getPickupAddress())
                .pickupLat(p.getPickupLat())
                .pickupLng(p.getPickupLng())
                .dropAddress(p.getDropAddress())
                .dropLat(p.getDropLat())
                .dropLng(p.getDropLng())
                .weightKg(p.getWeightKg())
                .size(p.getSize())
                .serviceTier(p.getServiceTier())
                .price(p.getPrice())
                .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                .build();
    }

    private String actionNote(String action) {
        return switch (action) {
            case "pickup" -> "Picked up from sender";
            case "in-transit" -> "In transit to receiver";
            case "arrived" -> "Arrived at receiver location";
            default -> action;
        };
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
package com.webtrans.Courier.service;

import com.webtrans.Courier.dto.partner.CreatePartnerRequest;
import com.webtrans.Courier.dto.partner.LocationUpdateRequest;
import com.webtrans.Courier.dto.partner.PartnerResponse;
import com.webtrans.Courier.model.CourierUser;
import com.webtrans.Courier.model.Partner;
import com.webtrans.Courier.repository.CourierUserRepository;
import com.webtrans.Courier.repository.PartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PartnerService {

    @Autowired private PartnerRepository partnerRepository;
    @Autowired private CourierUserRepository userRepository;
    @Autowired private com.webtrans.auth.repository.UserRepository sharedUserRepository;
    @Autowired private CourierProfileService courierProfileService;

    // ==================== REGISTER AS PARTNER ====================
    @Transactional
    public PartnerResponse registerAsPartner(CreatePartnerRequest request, String userEmail) {

        CourierUser user = userRepository.findByEmail(userEmail)
                .orElseGet(() -> {
                    com.webtrans.auth.model.User shared = sharedUserRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
                    return courierProfileService.resolveProfile(shared).getProfile();
                });

        // Onboard user as PARTNER if they are currently SENDER
        if (user.getRole() != CourierUser.Role.PARTNER) {
            user.setRole(CourierUser.Role.PARTNER);
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone().trim());
        }
        userRepository.save(user);

        // Update vehicle type if already registered as a partner
        if (partnerRepository.existsByUserId(user.getId())) {
            Partner existing = partnerRepository.findByUserId(user.getId()).orElseThrow();
            if (request.getVehicleType() != null) {
                existing.setVehicleType(request.getVehicleType());
                existing = partnerRepository.save(existing);
            }
            return toResponse(existing);
        }

        Partner partner = Partner.builder()
                .user(user)
                .vehicleType(request.getVehicleType() != null ? request.getVehicleType() : Partner.VehicleType.BIKE)
                .isOnline(false)
                .isAvailable(true)
                .rating(BigDecimal.valueOf(5.00))
                .build();

        Partner saved = partnerRepository.save(partner);
        return toResponse(saved);
    }

    // ==================== GET PROFILE ====================
    public PartnerResponse getMyProfile(String userEmail) {
        Partner partner = findPartnerByEmail(userEmail);
        return toResponse(partner);
    }

    // ==================== TOGGLE ONLINE ====================
    @Transactional
    public PartnerResponse setOnlineStatus(String userEmail, boolean isOnline) {
        Partner partner = findPartnerByEmail(userEmail);
        partner.setIsOnline(isOnline);

        // When going offline, mark unavailable too
        if (!isOnline) {
            partner.setIsAvailable(false);
        } else {
            partner.setIsAvailable(true);
        }

        Partner saved = partnerRepository.save(partner);
        return toResponse(saved);
    }

    // ==================== UPDATE LOCATION ====================
    @Transactional
    public PartnerResponse updateLocation(String userEmail, LocationUpdateRequest request) {
        Partner partner = findPartnerByEmail(userEmail);
        partner.setCurrentLat(request.getLat());
        partner.setCurrentLng(request.getLng());
        Partner saved = partnerRepository.save(partner);
        return toResponse(saved);
    }

    // ==================== HELPERS ====================
    private Partner findPartnerByEmail(String email) {
        CourierUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return partnerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "You are not registered as a delivery partner yet. " +
                                "Call POST /api/courier/partner/register first."));
    }

    private PartnerResponse toResponse(Partner p) {
        return PartnerResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .name(p.getUser().getName())
                .email(p.getUser().getEmail())
                .phone(p.getUser().getPhone())
                .vehicleType(p.getVehicleType())
                .isOnline(p.getIsOnline())
                .isAvailable(p.getIsAvailable())
                .currentLat(p.getCurrentLat())
                .currentLng(p.getCurrentLng())
                .rating(p.getRating())
                .build();
    }
}
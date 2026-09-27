package com.seproject.courier.service;

import com.seproject.courier.dto.partner.CreatePartnerRequest;
import com.seproject.courier.dto.partner.LocationUpdateRequest;
import com.seproject.courier.dto.partner.PartnerResponse;
import com.seproject.courier.entity.Partner;
import com.seproject.courier.entity.User;
import com.seproject.courier.repository.PartnerRepository;
import com.seproject.courier.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PartnerService {

    @Autowired private PartnerRepository partnerRepository;
    @Autowired private UserRepository userRepository;

    // ==================== REGISTER AS PARTNER ====================
    @Transactional
    public PartnerResponse registerAsPartner(CreatePartnerRequest request, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        // Must be a PARTNER-role user
        if (user.getRole() != User.Role.PARTNER) {
            throw new RuntimeException("Only PARTNER-role users can register as delivery partners");
        }

        // Prevent duplicate partner registration
        if (partnerRepository.existsByUserId(user.getId())) {
            throw new RuntimeException("User is already registered as a partner");
        }

        Partner partner = Partner.builder()
                .user(user)
                .vehicleType(request.getVehicleType())
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
        User user = userRepository.findByEmail(email)
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
package com.webtrans.Courier.service;

import com.webtrans.Courier.model.CourierUser;
import com.webtrans.Courier.model.Partner;
import com.webtrans.Courier.repository.CourierUserRepository;
import com.webtrans.Courier.repository.PartnerRepository;
import com.webtrans.auth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("courierProfileService")
public class CourierProfileService {

    @Autowired
    private CourierUserRepository courierUserRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public enum ResolutionStatus {
        SUCCESS,
        AMBIGUOUS_UNLINKED_ACCOUNT,
        UNAUTHENTICATED
    }

    public static class ProfileResolutionResult {
        private final ResolutionStatus status;
        private final CourierUser profile;
        private final String message;

        public ProfileResolutionResult(ResolutionStatus status, CourierUser profile, String message) {
            this.status = status;
            this.profile = profile;
            this.message = message;
        }

        public ResolutionStatus getStatus() { return status; }
        public CourierUser getProfile() { return profile; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return status == ResolutionStatus.SUCCESS; }
    }

    /**
     * Resolves or safely provisions a CourierUser for the authenticated shared User.
     * Prevents duplicate requests and does NOT automatically link legacy Courier accounts.
     */
    @Transactional
    public synchronized ProfileResolutionResult resolveProfile(User sharedUser) {
        if (sharedUser == null || sharedUser.getId() == null) {
            return new ProfileResolutionResult(ResolutionStatus.UNAUTHENTICATED, null, "No active user session.");
        }

        // 1. Check existing verified link by shared user ID
        Optional<CourierUser> linkedOpt = courierUserRepository.findBySharedUserId(sharedUser.getId());
        if (linkedOpt.isPresent()) {
            return new ProfileResolutionResult(ResolutionStatus.SUCCESS, linkedOpt.get(), "Profile resolved successfully.");
        }

        // 2. Check for legacy unlinked account with the same email
        String email = sharedUser.getEmail() != null ? sharedUser.getEmail().trim().toLowerCase() : "";
        Optional<CourierUser> legacyOpt = courierUserRepository.findByEmail(email);
        if (legacyOpt.isPresent()) {
            CourierUser legacy = legacyOpt.get();
            if (legacy.getSharedUserId() == null) {
                return new ProfileResolutionResult(
                        ResolutionStatus.AMBIGUOUS_UNLINKED_ACCOUNT,
                        legacy,
                        "An existing legacy Courier account was found for " + email + ". Explicit password verification is required to link."
                );
            }
        }

        // 3. Auto-provision a basic sender profile for the shared user
        String name = sharedUser.getFullName();
        if (name == null || name.isBlank()) {
            name = "NEXT GO User";
        }

        CourierUser newProfile = CourierUser.builder()
                .name(name.trim())
                .email(email)
                .sharedUserId(sharedUser.getId())
                .role(CourierUser.Role.SENDER)
                .passwordHash(null)
                .build();

        CourierUser saved = courierUserRepository.save(newProfile);
        return new ProfileResolutionResult(ResolutionStatus.SUCCESS, saved, "New sender profile provisioned.");
    }

    /**
     * Verified linking process: links an existing legacy Courier account to the shared user
     * after validating the legacy password.
     */
    @Transactional
    public boolean linkLegacyAccount(User sharedUser, String legacyPassword) {
        if (sharedUser == null || legacyPassword == null || legacyPassword.isBlank()) {
            return false;
        }

        String email = sharedUser.getEmail() != null ? sharedUser.getEmail().trim().toLowerCase() : "";
        Optional<CourierUser> legacyOpt = courierUserRepository.findByEmail(email);
        if (legacyOpt.isEmpty()) {
            return false;
        }

        CourierUser legacy = legacyOpt.get();
        if (legacy.getSharedUserId() != null && !legacy.getSharedUserId().equals(sharedUser.getId())) {
            // Already linked to a different shared user
            return false;
        }

        if (legacy.getPasswordHash() != null && passwordEncoder.matches(legacyPassword, legacy.getPasswordHash())) {
            legacy.setSharedUserId(sharedUser.getId());
            courierUserRepository.save(legacy);
            return true;
        }

        return false;
    }

    /**
     * Partner onboarding under the existing shared account without requiring separate credentials.
     */
    @Transactional
    public Partner onboardPartner(User sharedUser, Partner.VehicleType vehicleType, String phone) {
        ProfileResolutionResult res = resolveProfile(sharedUser);
        if (!res.isSuccess()) {
            throw new IllegalStateException("Cannot onboard partner: " + res.getMessage());
        }

        CourierUser user = res.getProfile();
        user.setRole(CourierUser.Role.PARTNER);
        if (phone != null && !phone.isBlank()) {
            user.setPhone(phone.trim());
        }
        courierUserRepository.save(user);

        Partner partner = partnerRepository.findByUserId(user.getId())
                .orElseGet(() -> Partner.builder()
                        .user(user)
                        .vehicleType(vehicleType != null ? vehicleType : Partner.VehicleType.BIKE)
                        .isOnline(false)
                        .isAvailable(true)
                        .build());

        if (vehicleType != null) {
            partner.setVehicleType(vehicleType);
        }

        return partnerRepository.save(partner);
    }
}

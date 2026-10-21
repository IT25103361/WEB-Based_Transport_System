package com.webtrans.ridebook.service;

import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import com.webtrans.auth.service.PasswordService;
import com.webtrans.ridebook.model.Driver;
import com.webtrans.ridebook.repository.DriverRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves only driver profiles linked to the authenticated account and
 * manages atomic driver registration and profile resolution.
 */
@Service
public class DriverProfileService {
    private static final String SELECTED_PROFILE = "rideSelectedDriverId";

    @Autowired
    private DriverRepository drivers;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    public DriverProfileService() {}

    public DriverProfileService(DriverRepository drivers) {
        this.drivers = drivers;
    }

    public enum ProfileResolutionStatus {
        SUCCESS,
        NO_PROFILE,
        MULTIPLE_PROFILES_CONFLICT
    }

    public static class DriverResolutionResult {
        private final ProfileResolutionStatus status;
        private final Driver driver;
        private final List<Driver> conflictingDrivers;
        private final String message;

        public DriverResolutionResult(ProfileResolutionStatus status, Driver driver, List<Driver> conflictingDrivers, String message) {
            this.status = status;
            this.driver = driver;
            this.conflictingDrivers = conflictingDrivers;
            this.message = message;
        }

        public ProfileResolutionStatus getStatus() { return status; }
        public Driver getDriver() { return driver; }
        public List<Driver> getConflictingDrivers() { return conflictingDrivers; }
        public String getMessage() { return message; }
    }

    /**
     * Resolves the linked driver profile for a specific user ID.
     * Safely detects legacy conflicts (multiple driver profiles linked to one account)
     * without guessing, auto-merging, or arbitrarily picking one.
     */
    public DriverResolutionResult resolveDriverProfile(Long userId) {
        if (userId == null) {
            return new DriverResolutionResult(ProfileResolutionStatus.NO_PROFILE, null, null, "User not authenticated.");
        }

        List<Driver> linked = drivers.findAllByUserIdOrderByIdAsc(userId);
        if (linked == null || linked.isEmpty()) {
            return new DriverResolutionResult(ProfileResolutionStatus.NO_PROFILE, null, null,
                    "No driver profile is linked to this account. Please register as a driver.");
        }

        if (linked.size() == 1) {
            return new DriverResolutionResult(ProfileResolutionStatus.SUCCESS, linked.get(0), null, "Driver profile resolved.");
        }

        // Multiple profiles detected: explicit conflict
        String ids = linked.stream().map(d -> "#" + d.getId()).collect(Collectors.joining(", "));
        return new DriverResolutionResult(
                ProfileResolutionStatus.MULTIPLE_PROFILES_CONFLICT,
                null,
                linked,
                "Multiple driver profiles detected for your account (" + ids + "). " +
                        "To protect account isolation, please run the database reconciliation procedure before logging in."
        );
    }

    /**
     * Atomically creates a User account and a linked Driver profile.
     * A failed registration rolls back completely, preventing orphaned driver records or partial accounts.
     */
    @Transactional
    public Driver registerDriver(String fullName, String email, String rawPassword,
                                 String phoneNumber, String vehicleNumber, String vehicleType) {
        if (fullName == null || fullName.isBlank() ||
                email == null || email.isBlank() ||
                rawPassword == null || rawPassword.isBlank() ||
                phoneNumber == null || phoneNumber.isBlank() ||
                vehicleNumber == null || vehicleNumber.isBlank() ||
                vehicleType == null || vehicleType.isBlank()) {
            throw new IllegalArgumentException("All driver registration fields are required.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        // 1. Create and save User with BCrypt password
        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordService.encode(rawPassword));
        User savedUser = userRepository.save(user);

        // 2. Atomically create linked Driver profile
        Driver driver = new Driver();
        driver.setUserId(savedUser.getId());
        driver.setDriverName(fullName.trim());
        driver.setPhoneNumber(phoneNumber.trim());
        driver.setVehicleNumber(vehicleNumber.trim());
        driver.setVehicleType(vehicleType.trim());
        driver.setStatus("AVAILABLE");

        return drivers.save(driver);
    }

    public List<Driver> linkedProfiles(HttpSession session) {
        User user = (User) session.getAttribute("driverUser");
        if (user == null || user.getId() == null) return List.of();
        return drivers.findAllByUserIdOrderByIdAsc(user.getId());
    }

    public Driver currentProfile(HttpSession session) {
        List<Driver> profiles = linkedProfiles(session);
        Object selected = session.getAttribute(SELECTED_PROFILE);
        for (Driver driver : profiles) {
            if (driver.getId().equals(selected)) return driver;
        }
        session.removeAttribute(SELECTED_PROFILE);
        if (profiles.size() == 1) {
            Driver driver = profiles.get(0);
            session.setAttribute(SELECTED_PROFILE, driver.getId());
            return driver;
        }
        return null;
    }

    public Driver requireProfile(HttpSession session) {
        if (session.getAttribute("driverUser") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        Driver driver = currentProfile(session);
        if (driver == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No resolved driver profile found for this account.");
        }
        return driver;
    }

    public void selectProfile(HttpSession session, Long id) {
        for (Driver driver : linkedProfiles(session)) {
            if (driver.getId().equals(id)) {
                session.setAttribute(SELECTED_PROFILE, id);
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "This driver profile is not linked to your account.");
    }
}
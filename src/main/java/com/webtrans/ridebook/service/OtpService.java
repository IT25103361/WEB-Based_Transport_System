package com.seproject.courier.service;

import com.seproject.courier.entity.Otp;
import com.seproject.courier.entity.Parcel;
import com.seproject.courier.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 60;
    private static final int MAX_GENERATION_ATTEMPTS = 20;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Generate (or regenerate) an OTP for the parcel.
     * The 6-digit code is verified against the DB to avoid collisions.
     * Then the code is sent to the receiver (console copy + real email if configured).
     */
    @Transactional
    public Otp generateForParcel(Parcel parcel) {

        // If an unverified OTP already exists and is still valid, reuse it
        // instead of generating a new one (prevents email/dashboard mismatch).
        Otp existing = otpRepository.findByParcelId(parcel.getId()).orElse(null);
        if (existing != null
                && !Boolean.TRUE.equals(existing.getVerified())
                && existing.getExpiresAt() != null
                && existing.getExpiresAt().isAfter(LocalDateTime.now())) {
            return existing;
        }

        // Otherwise generate a fresh code
        String code = generateUniqueCode();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

        Otp otp = existing != null
                ? existing
                : Otp.builder().parcel(parcel).build();

        otp.setOtpCode(code);
        otp.setExpiresAt(expiry);
        otp.setVerified(false);

        Otp saved = otpRepository.save(otp);

        // Send to receiver — console always, real email if configured
        emailService.sendDeliveryOtp(parcel, code);

        return saved;
    }
    /**
     * Verify an OTP for a given parcel. Returns true if valid + not expired + not already used.
     */
    @Transactional
    public boolean verify(Parcel parcel, String submittedCode) {
        Otp otp = otpRepository.findByParcelId(parcel.getId())
                .orElseThrow(() -> new RuntimeException("No OTP exists for this parcel"));

        if (Boolean.TRUE.equals(otp.getVerified())) {
            throw new RuntimeException("OTP has already been used");
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Request a new one.");
        }

        if (!otp.getOtpCode().equals(submittedCode)) {
            throw new RuntimeException("Invalid OTP");
        }

        otp.setVerified(true);
        otpRepository.save(otp);
        return true;
    }

    // ==================== HELPERS ====================

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_GENERATION_ATTEMPTS; i++) {
            String candidate = randomDigits(OTP_LENGTH);
            if (otpRepository.findByOtpCode(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new RuntimeException("Could not generate a unique OTP after " + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    private String randomDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
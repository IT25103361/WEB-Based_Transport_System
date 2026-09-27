package com.seproject.courier.repository;

import com.seproject.courier.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByParcelId(Long parcelId);

    Optional<Otp> findByOtpCode(String otpCode);

    boolean existsByParcelId(Long parcelId);
}
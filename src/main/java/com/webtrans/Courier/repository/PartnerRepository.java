package com.webtrans.Courier.repository;

import com.webtrans.Courier.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    Optional<Partner> findByUserId(Long userId);

    // Used for nearest-partner matching
    List<Partner> findByIsOnlineTrueAndIsAvailableTrue();

    // Used when accepting a parcel
    boolean existsByUserId(Long userId);
}
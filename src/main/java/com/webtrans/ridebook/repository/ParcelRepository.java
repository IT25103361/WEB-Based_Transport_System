package com.seproject.courier.repository;

import com.seproject.courier.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    Optional<Parcel> findByTrackingCode(String trackingCode);

    List<Parcel> findBySenderIdOrderByCreatedAtDesc(Long senderId);

    // Partner sees parcels waiting for a match
    List<Parcel> findByStatus(Parcel.Status status);

    // Sender sees parcels by status
    List<Parcel> findBySenderIdAndStatus(Long senderId, Parcel.Status status);
}
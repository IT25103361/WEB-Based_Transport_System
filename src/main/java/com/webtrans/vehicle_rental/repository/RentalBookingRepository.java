package com.webtrans.vehicle_rental.repository;

import com.webtrans.vehicle_rental.model.RentalBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalBookingRepository extends JpaRepository<RentalBooking, Long> {

    List<RentalBooking> findByCustomerUser_IdOrderByIdDesc(Long customerUserId);

    List<RentalBooking> findAllByOrderByIdDesc();

    List<RentalBooking> findByVehicle_IdAndStatusIn(Long vehicleId, List<String> statuses);

    List<RentalBooking> findByVehicle_IdAndIdNotAndStatusIn(Long vehicleId, Long excludeId, List<String> statuses);

    // Admin dashboard read-only queries
    long countByStatus(String status);

    long countByExtendedFromDateIsNotNull();

    List<RentalBooking> findTop20ByOrderByIdDesc();

    List<RentalBooking> findByStartDateBetweenOrEndDateBetween(java.time.LocalDate s1, java.time.LocalDate e1, java.time.LocalDate s2, java.time.LocalDate e2);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.totalCost), 0) FROM RentalBooking b WHERE b.status IN ('CONFIRMED', 'COMPLETED')")
    Double sumConfirmedOrCompletedTotalCost();
}


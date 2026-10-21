package com.webtrans.ridebook.repository;

import com.webtrans.auth.model.User;
import com.webtrans.ridebook.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByPassengerUser(User passengerUser);

    List<Booking> findByPassengerUser_Id(Long passengerUserId);

    List<Booking> findByPassengerUser_IdOrderByIdDesc(Long passengerUserId);

    List<Booking> findByPassengerUser_IdAndStatusOrderByIdDesc(Long passengerUserId, String status);

    long countByPassengerUser_Id(Long passengerUserId);

    // Queries for Driver Portal
    List<Booking> findByStatusAndDriverIsNullOrderByIdDesc(String status);

    List<Booking> findByDriver_IdOrderByIdDesc(Long driverId);

    // Admin read-only dashboard queries
    long countByStatus(String status);

    List<Booking> findTop20ByOrderByIdDesc();

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.fare), 0) FROM Booking b WHERE b.status = :status")
    Double sumFareByStatus(@org.springframework.data.repository.query.Param("status") String status);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.fare), 0) FROM Booking b WHERE b.status NOT IN ('CANCELLED', 'PAID')")
    Double sumEstimatedPendingFares();
}
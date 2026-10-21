package com.webtrans.vehicle_rental.repository;

import com.webtrans.vehicle_rental.model.RentalVehicle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalVehicleRepository extends JpaRepository<RentalVehicle, Long> {
    List<RentalVehicle> findByStatus(String status);

    List<RentalVehicle> findAllByOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM RentalVehicle v WHERE v.id = :id")
    Optional<RentalVehicle> findByIdWithLock(@Param("id") Long id);

    long countByStatus(String status);
}


package com.webtrans.ridebook.repository;

import com.webtrans.ridebook.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    // Free inna driversla witharak hoyaganna
    List<Driver> findByStatus(String status);
}
package com.webtrans.ridebook.repository;

import com.webtrans.ridebook.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByStatus(String status);

    Driver findByUserId(Long userId);

    List<Driver> findAllByUserId(Long userId);

    List<Driver> findAllByUserIdOrderByIdAsc(Long userId);
    long countByStatus(String status);
    List<Driver> findTop20ByOrderByIdDesc();
}
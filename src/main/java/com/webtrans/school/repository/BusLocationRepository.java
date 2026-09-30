package com.webtrans.school.repository;

import com.webtrans.school.model.BusLocation;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface BusLocationRepository extends JpaRepository<BusLocation, Long> {
    List<BusLocation> findByBusIdOrderByRecordedAtDesc(Long busId);
    Optional<BusLocation> findTopByBusIdOrderByRecordedAtDesc(Long busId);
}
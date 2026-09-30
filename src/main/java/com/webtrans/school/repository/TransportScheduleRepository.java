package com.webtrans.school.repository;

import com.webtrans.school.model.TransportSchedule;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface TransportScheduleRepository extends JpaRepository<TransportSchedule, Long> {
    Optional<TransportSchedule> findByStudentId(Long studentId);
    List<TransportSchedule> findByBusId(Long busId);
}
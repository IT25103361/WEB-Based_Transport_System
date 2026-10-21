package com.webtrans.school.repository;

import com.webtrans.school.model.StudentArrival;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface StudentArrivalRepository extends JpaRepository<StudentArrival, Long> {
    Optional<StudentArrival> findTopByStudentIdOrderByArrivalTimeDesc(Long studentId);
    List<StudentArrival> findTop20ByOrderByIdDesc();
    List<StudentArrival> findByArrivalTimeBetweenOrderByArrivalTimeDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
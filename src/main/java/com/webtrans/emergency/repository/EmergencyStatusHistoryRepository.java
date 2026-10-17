package com.webtrans.emergency.repository;

import com.webtrans.emergency.model.EmergencyStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyStatusHistoryRepository extends JpaRepository<EmergencyStatusHistory, Long> {

    List<EmergencyStatusHistory> findByEmergencyRequestIdOrderByCreatedAtAsc(Long requestId);
}

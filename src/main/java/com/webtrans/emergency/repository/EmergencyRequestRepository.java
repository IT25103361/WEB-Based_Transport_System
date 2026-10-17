package com.webtrans.emergency.repository;

import com.webtrans.emergency.model.EmergencyRequest;
import com.webtrans.emergency.model.EmergencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {

    Optional<EmergencyRequest> findByTrackingToken(String trackingToken);

    Optional<EmergencyRequest> findByTrackingTokenAndIsDeletedFalse(String trackingToken);

    Optional<EmergencyRequest> findByIdempotencyKey(String idempotencyKey);

    List<EmergencyRequest> findAllByOrderByCreatedAtDesc();

    List<EmergencyRequest> findAllByIsDeletedFalseOrderByCreatedAtDesc();

    long countByStatus(EmergencyStatus status);

    long countByStatusAndIsDeletedFalse(EmergencyStatus status);

    long countByCreatedAtAfter(LocalDateTime time);
}

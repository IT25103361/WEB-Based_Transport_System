package com.webtrans.Courier.repository;

import com.webtrans.Courier.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Optional<Assignment> findByParcelId(Long parcelId);

    List<Assignment> findByPartnerId(Long partnerId);

    boolean existsByParcelId(Long parcelId);
}
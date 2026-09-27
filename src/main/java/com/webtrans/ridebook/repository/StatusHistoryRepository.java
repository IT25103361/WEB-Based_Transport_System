package com.seproject.courier.repository;

import com.seproject.courier.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findByParcelIdOrderByChangedAtAsc(Long parcelId);
}
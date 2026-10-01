package com.webtrans.Courier.repository;

import com.webtrans.Courier.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findByParcelIdOrderByChangedAtAsc(Long parcelId);
}
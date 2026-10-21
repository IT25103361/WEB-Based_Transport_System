package com.webtrans.foodgrocery.repository;

import com.webtrans.foodgrocery.entity.FoodOrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodOrderStatusHistoryRepository extends JpaRepository<FoodOrderStatusHistory, Long> {

    List<FoodOrderStatusHistory> findByOrderIdOrderByTimestampAsc(Long orderId);
}

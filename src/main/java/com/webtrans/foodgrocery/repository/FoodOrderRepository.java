package com.webtrans.foodgrocery.repository;

import com.webtrans.foodgrocery.entity.FoodOrder;
import com.webtrans.foodgrocery.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long>, JpaSpecificationExecutor<FoodOrder> {

    Page<FoodOrder> findByCustomerIdOrderByPlacedAtDesc(Long customerId, Pageable pageable);

    List<FoodOrder> findByStatusOrderByPlacedAtAsc(OrderStatus status);

    List<FoodOrder> findByStatus(OrderStatus status);

    List<FoodOrder> findByShopId(Long shopId);
}

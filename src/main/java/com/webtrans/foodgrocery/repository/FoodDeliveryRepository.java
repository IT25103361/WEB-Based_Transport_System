package com.webtrans.foodgrocery.repository;

import com.webtrans.foodgrocery.entity.FoodDelivery;
import com.webtrans.foodgrocery.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodDeliveryRepository extends JpaRepository<FoodDelivery, Long> {

    Optional<FoodDelivery> findByOrderId(Long orderId);

    List<FoodDelivery> findByDeliveryPartnerId(Long deliveryPartnerId);

    List<FoodDelivery> findByStatus(DeliveryStatus status);
}

package com.webtrans.foodgrocery.repository;

import com.webtrans.foodgrocery.entity.FoodDeliveryPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodDeliveryPersonRepository extends JpaRepository<FoodDeliveryPerson, Long> {

    Optional<FoodDeliveryPerson> findByUserId(Long userId);

    List<FoodDeliveryPerson> findByIsAvailableTrue();
}

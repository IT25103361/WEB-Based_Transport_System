package com.webtrans.foodgrocery.repository;

import com.webtrans.foodgrocery.entity.FoodShop;
import com.webtrans.foodgrocery.enums.VendorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodShopRepository extends JpaRepository<FoodShop, Long> {

    List<FoodShop> findByActiveTrue();

    List<FoodShop> findByTypeAndActiveTrue(VendorType type);

    List<FoodShop> findByNameContainingIgnoreCase(String name);

    List<FoodShop> findByCityContainingIgnoreCase(String city);

    List<FoodShop> findByManagerId(Long managerId);
}

package com.webtrans.foodgrocery.repository;

import com.webtrans.foodgrocery.entity.FoodVendor;
import com.webtrans.foodgrocery.enums.VendorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodVendorRepository extends JpaRepository<FoodVendor, Long> {

    List<FoodVendor> findByActiveTrue();

    List<FoodVendor> findByTypeAndActiveTrue(VendorType type);

    List<FoodVendor> findByNameContainingIgnoreCase(String name);

    List<FoodVendor> findByManagerId(Long managerId);
}

package com.webtrans.foodgrocery.repository;

import com.webtrans.foodgrocery.entity.FoodProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodProductRepository extends JpaRepository<FoodProduct, Long>, JpaSpecificationExecutor<FoodProduct> {

    List<FoodProduct> findByVendorIdAndAvailableTrue(Long vendorId);

    List<FoodProduct> findByVendorIdAndCategory(Long vendorId, String category);

    List<FoodProduct> findByPopularTrue();

    List<FoodProduct> findByNameContainingIgnoreCase(String name);

    List<FoodProduct> findByShopIdAndAvailableTrue(Long shopId);

    List<FoodProduct> findByCategoryContainingIgnoreCase(String category);
}

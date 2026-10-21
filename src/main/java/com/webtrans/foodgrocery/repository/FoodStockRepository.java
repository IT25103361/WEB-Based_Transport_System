package com.webtrans.foodgrocery.repository;

import com.webtrans.foodgrocery.entity.FoodStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodStockRepository extends JpaRepository<FoodStock, Long> {

    List<FoodStock> findByShopId(Long shopId);

    Optional<FoodStock> findByProductIdAndShopId(Long productId, Long shopId);

    List<FoodStock> findByProductId(Long productId);

    @Query("SELECT s FROM FoodStock s WHERE s.quantity <= s.minStockLevel")
    List<FoodStock> findLowStockItems();

    @Query("SELECT s FROM FoodStock s WHERE s.shop.id = :shopId AND s.quantity <= s.minStockLevel")
    List<FoodStock> findLowStockByShop(@Param("shopId") Long shopId);
}

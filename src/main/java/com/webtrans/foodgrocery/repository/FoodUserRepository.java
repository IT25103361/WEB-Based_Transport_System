package com.webtrans.foodgrocery.repository;

import com.webtrans.foodgrocery.entity.FoodUser;
import com.webtrans.foodgrocery.enums.FoodRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodUserRepository extends JpaRepository<FoodUser, Long> {

    Optional<FoodUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<FoodUser> findByRole(FoodRole role);

    Optional<FoodUser> findByNextgoUserId(Long nextgoUserId);
}

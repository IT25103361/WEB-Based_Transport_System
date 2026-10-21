package com.webtrans.Courier.repository;

import com.webtrans.Courier.model.CourierUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("courierUserRepository")
public interface CourierUserRepository extends JpaRepository<CourierUser, Long> {

    Optional<CourierUser> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<CourierUser> findBySharedUserId(Long sharedUserId);

    boolean existsBySharedUserId(Long sharedUserId);
}

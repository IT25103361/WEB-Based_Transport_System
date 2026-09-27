package com.seproject.courier.repository;

import com.seproject.courier.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByParcelId(Long parcelId);

    boolean existsByParcelId(Long parcelId);
}
package com.emergency.service.repository;

import com.emergency.service.model.EmergencyService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyServiceRepository extends JpaRepository<EmergencyService, Long> {
}

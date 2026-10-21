package com.webtrans.ridebook.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RideFareCalculator {

    private final Map<String, RideFareStrategy> strategies = new HashMap<>();
    private final RideFareStrategy defaultStrategy;

    @Autowired
    public RideFareCalculator(List<RideFareStrategy> strategyList) {
        RideFareStrategy fallback = null;
        if (strategyList != null) {
            for (RideFareStrategy s : strategyList) {
                registerStrategy(s);
                if ("Car".equalsIgnoreCase(s.getVehicleType())) {
                    fallback = s;
                }
            }
        }
        this.defaultStrategy = fallback != null ? fallback : new CarFareStrategy();
    }

    public void registerStrategy(RideFareStrategy strategy) {
        if (strategy == null) return;
        String key = normalize(strategy.getVehicleType());
        strategies.put(key, strategy);

        // Normalize aliases for 3-wheeler variants
        if (key.contains("THREE") || key.contains("WHEEL")) {
            strategies.put("THREEWHEELER", strategy);
            strategies.put("THREE-WHEELER", strategy);
            strategies.put("WHEEL", strategy);
        }
    }

    public RideFareStrategy getStrategy(String vehicleType) {
        if (vehicleType == null || vehicleType.isBlank()) {
            return defaultStrategy;
        }
        String key = normalize(vehicleType);
        return strategies.getOrDefault(key, defaultStrategy);
    }

    public double calculateFare(String vehicleType, double distanceKm) {
        RideFareStrategy strategy = getStrategy(vehicleType);
        return strategy.calculateFare(distanceKm);
    }

    public Map<String, Double> calculateAllFares(double distanceKm) {
        Map<String, Double> result = new LinkedHashMap<>();
        // Fixed standard ordering
        List<String> displayOrder = List.of("Bike", "Three-Wheeler", "Car", "Van");
        for (String type : displayOrder) {
            RideFareStrategy strategy = getStrategy(type);
            result.put(strategy.getVehicleType(), strategy.calculateFare(distanceKm));
        }
        return result;
    }

    private String normalize(String type) {
        return type == null ? "" : type.trim().toUpperCase().replace("-", "").replace("_", "").replace(" ", "");
    }
}

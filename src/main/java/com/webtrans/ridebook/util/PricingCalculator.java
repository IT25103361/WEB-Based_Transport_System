package com.seproject.courier.util;

import com.seproject.courier.entity.Parcel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PricingCalculator {

    // Base rates (LKR) — tweak as needed for your demo
    private static final BigDecimal INSTANT_BASE = BigDecimal.valueOf(300);
    private static final BigDecimal INSTANT_PER_KM = BigDecimal.valueOf(60);

    private static final BigDecimal ISLANDWIDE_BASE = BigDecimal.valueOf(200);
    private static final BigDecimal ISLANDWIDE_PER_KG = BigDecimal.valueOf(50);

    /**
     * Compute the price for a parcel.
     *
     * INSTANT    → base + (distance_km * rate_per_km)
     * ISLANDWIDE → base + (weight_kg * rate_per_kg)
     */
    public BigDecimal calculate(Parcel parcel) {

        BigDecimal price;

        if (parcel.getServiceTier() == Parcel.ServiceTier.INSTANT) {
            double distanceKm = haversineKm(
                    parcel.getPickupLat().doubleValue(),
                    parcel.getPickupLng().doubleValue(),
                    parcel.getDropLat().doubleValue(),
                    parcel.getDropLng().doubleValue()
            );
            price = INSTANT_BASE.add(
                    INSTANT_PER_KM.multiply(BigDecimal.valueOf(distanceKm))
            );

        } else { // ISLANDWIDE
            BigDecimal weight = parcel.getWeightKg() == null
                    ? BigDecimal.ONE
                    : parcel.getWeightKg();
            price = ISLANDWIDE_BASE.add(
                    ISLANDWIDE_PER_KG.multiply(weight)
            );
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    /** Haversine distance between two GPS points, in kilometers. */
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
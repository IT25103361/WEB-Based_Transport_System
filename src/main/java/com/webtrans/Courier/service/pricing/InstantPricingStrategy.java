package com.webtrans.Courier.service.pricing;

import com.webtrans.Courier.model.Parcel;
import com.webtrans.Courier.service.MatchingService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * INSTANT pricing: base fare + per-kilometre rate.
 * Distance = haversine between pickup pin and drop pin.
 */
@Component
public class InstantPricingStrategy implements PricingStrategy {

    private static final BigDecimal BASE_FARE = BigDecimal.valueOf(300);
    private static final BigDecimal PER_KM = BigDecimal.valueOf(60);

    @Override
    public Parcel.ServiceTier supports() {
        return Parcel.ServiceTier.INSTANT;
    }

    @Override
    public BigDecimal calculate(Parcel parcel) {
        double km = MatchingService.haversineKm(
                parcel.getPickupLat().doubleValue(),
                parcel.getPickupLng().doubleValue(),
                parcel.getDropLat().doubleValue(),
                parcel.getDropLng().doubleValue()
        );

        // Price = base + 60 * km, rounded to 2 decimals
        return BASE_FARE
                .add(PER_KM.multiply(BigDecimal.valueOf(km)))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
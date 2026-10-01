package com.webtrans.Courier.service.pricing;

import com.webtrans.Courier.model.Parcel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ISLANDWIDE pricing: base fare + per-kilogram rate.
 * Distance doesn't matter for islandwide.
 */
@Component
public class IslandwidePricingStrategy implements PricingStrategy {

    private static final BigDecimal BASE_FARE = BigDecimal.valueOf(200);
    private static final BigDecimal PER_KG = BigDecimal.valueOf(50);

    @Override
    public Parcel.ServiceTier supports() {
        return Parcel.ServiceTier.ISLANDWIDE;
    }

    @Override
    public BigDecimal calculate(Parcel parcel) {
        BigDecimal weight = parcel.getWeightKg() != null
                ? parcel.getWeightKg()
                : BigDecimal.ONE;

        return BASE_FARE
                .add(PER_KG.multiply(weight))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
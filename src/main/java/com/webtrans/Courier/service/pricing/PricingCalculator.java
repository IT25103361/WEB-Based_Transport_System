package com.webtrans.Courier.service.pricing;

import com.webtrans.Courier.model.Parcel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Dispatcher: picks the correct PricingStrategy based on the parcel's service tier.
 * All strategies are auto-injected by Spring — no if/else here.
 */
@Component
public class PricingCalculator {

    private final Map<Parcel.ServiceTier, PricingStrategy> strategies;

    public PricingCalculator(List<PricingStrategy> strategies) {
        this.strategies = new EnumMap<>(Parcel.ServiceTier.class);
        for (PricingStrategy s : strategies) {
            this.strategies.put(s.supports(), s);
        }
    }

    public BigDecimal calculate(Parcel parcel) {
        PricingStrategy strategy = strategies.get(parcel.getServiceTier());
        if (strategy == null) {
            throw new IllegalStateException(
                    "No pricing strategy registered for tier: " + parcel.getServiceTier());
        }
        return strategy.calculate(parcel);
    }
}
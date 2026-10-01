package com.webtrans.Courier.service.pricing;

import com.webtrans.Courier.model.Parcel;

import java.math.BigDecimal;

/**
 * Strategy interface for pricing algorithms.
 * Each service tier (INSTANT, ISLANDWIDE, ...) has its own implementation.
 */
public interface PricingStrategy {

    /** Which service tier this strategy handles. */
    Parcel.ServiceTier supports();

    /** Calculate the price for the given parcel. */
    BigDecimal calculate(Parcel parcel);
}
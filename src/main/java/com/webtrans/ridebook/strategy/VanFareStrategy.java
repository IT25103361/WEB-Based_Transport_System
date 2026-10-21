package com.webtrans.ridebook.strategy;

import org.springframework.stereotype.Component;

@Component
public class VanFareStrategy extends AbstractRideFareStrategy {

    public static final String VEHICLE_TYPE = "Van";
    public static final double RATE_PER_KM = 210.0;
    public static final double MINIMUM_FARE = 210.0;

    public VanFareStrategy() {
        super(VEHICLE_TYPE, RATE_PER_KM, MINIMUM_FARE);
    }
}

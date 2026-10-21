package com.webtrans.ridebook.strategy;

import org.springframework.stereotype.Component;

@Component
public class ThreeWheelerFareStrategy extends AbstractRideFareStrategy {

    public static final String VEHICLE_TYPE = "Three-Wheeler";
    public static final double RATE_PER_KM = 120.0;
    public static final double MINIMUM_FARE = 120.0;

    public ThreeWheelerFareStrategy() {
        super(VEHICLE_TYPE, RATE_PER_KM, MINIMUM_FARE);
    }
}

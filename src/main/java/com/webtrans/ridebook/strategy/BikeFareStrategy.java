package com.webtrans.ridebook.strategy;

import org.springframework.stereotype.Component;

@Component
public class BikeFareStrategy extends AbstractRideFareStrategy {

    public static final String VEHICLE_TYPE = "Bike";
    public static final double RATE_PER_KM = 60.0;
    public static final double MINIMUM_FARE = 60.0;

    public BikeFareStrategy() {
        super(VEHICLE_TYPE, RATE_PER_KM, MINIMUM_FARE);
    }
}

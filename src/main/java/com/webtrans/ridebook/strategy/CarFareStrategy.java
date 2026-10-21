package com.webtrans.ridebook.strategy;

import org.springframework.stereotype.Component;

@Component
public class CarFareStrategy extends AbstractRideFareStrategy {

    public static final String VEHICLE_TYPE = "Car";
    public static final double RATE_PER_KM = 170.0;
    public static final double MINIMUM_FARE = 170.0;

    public CarFareStrategy() {
        super(VEHICLE_TYPE, RATE_PER_KM, MINIMUM_FARE);
    }
}

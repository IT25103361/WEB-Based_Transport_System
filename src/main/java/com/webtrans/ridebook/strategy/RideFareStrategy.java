package com.webtrans.ridebook.strategy;

public interface RideFareStrategy {

    String getVehicleType();

    double getRatePerKm();

    double getMinimumFare();

    double calculateFare(double distanceKm);
}

package com.webtrans.ridebook.strategy;

public abstract class AbstractRideFareStrategy implements RideFareStrategy {

    private final String vehicleType;
    private final double ratePerKm;
    private final double minimumFare;

    protected AbstractRideFareStrategy(String vehicleType, double ratePerKm, double minimumFare) {
        this.vehicleType = vehicleType;
        this.ratePerKm = ratePerKm;
        this.minimumFare = minimumFare;
    }

    @Override
    public String getVehicleType() {
        return vehicleType;
    }

    @Override
    public double getRatePerKm() {
        return ratePerKm;
    }

    @Override
    public double getMinimumFare() {
        return minimumFare;
    }

    @Override
    public double calculateFare(double distanceKm) {
        if (distanceKm < 1.0) {
            return minimumFare;
        }
        double calculated = Math.round(distanceKm * ratePerKm);
        return Math.max(calculated, minimumFare);
    }
}

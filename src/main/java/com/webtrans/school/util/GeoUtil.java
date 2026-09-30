package com.webtrans.school.util;

public final class GeoUtil {
    private GeoUtil() {}

    public static double metres(double a, double b, double c, double d) {
        double r = 6371000;
        double p = Math.PI / 180;
        double x = Math.sin((c - a) * p / 2);
        double y = Math.sin((d - b) * p / 2);
        return 2 * r * Math.asin(Math.sqrt(x * x + Math.cos(a * p) * Math.cos(c * p) * y * y));
    }

    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double p = Math.PI / 180.0;
        double dLon = (lon2 - lon1) * p;
        double y = Math.sin(dLon) * Math.cos(lat2 * p);
        double x = Math.cos(lat1 * p) * Math.sin(lat2 * p) - Math.sin(lat1 * p) * Math.cos(lat2 * p) * Math.cos(dLon);
        double deg = Math.toDegrees(Math.atan2(y, x));
        return (deg + 360.0) % 360.0;
    }
}
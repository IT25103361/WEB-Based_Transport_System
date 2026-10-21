package com.webtrans;

import com.webtrans.ridebook.controller.BookingController;
import com.webtrans.ridebook.model.Booking;
import com.webtrans.ridebook.strategy.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RideBookingStrategyTest {

    @Autowired
    private RideFareCalculator fareCalculator;

    @Autowired
    private BikeFareStrategy bikeFareStrategy;

    @Autowired
    private ThreeWheelerFareStrategy threeWheelerFareStrategy;

    @Autowired
    private CarFareStrategy carFareStrategy;

    @Autowired
    private VanFareStrategy vanFareStrategy;

    @Autowired
    private BookingController bookingController;

    @Test
    @DisplayName("1. Strategy Fare Rates: Correct per-km rates and rounding for each vehicle type")
    void testStrategyRatesAndCalculations() {
        double dist10Km = 10.0;

        // Bike: 60 LKR/km -> 600
        assertEquals(600.0, bikeFareStrategy.calculateFare(dist10Km));
        assertEquals("Bike", bikeFareStrategy.getVehicleType());
        assertEquals(60.0, bikeFareStrategy.getRatePerKm());

        // Three-Wheeler: 120 LKR/km -> 1200
        assertEquals(1200.0, threeWheelerFareStrategy.calculateFare(dist10Km));
        assertEquals("Three-Wheeler", threeWheelerFareStrategy.getVehicleType());
        assertEquals(120.0, threeWheelerFareStrategy.getRatePerKm());

        // Car: 170 LKR/km -> 1700
        assertEquals(1700.0, carFareStrategy.calculateFare(dist10Km));
        assertEquals("Car", carFareStrategy.getVehicleType());
        assertEquals(170.0, carFareStrategy.getRatePerKm());

        // Van: 210 LKR/km -> 2100
        assertEquals(2100.0, vanFareStrategy.calculateFare(dist10Km));
        assertEquals("Van", vanFareStrategy.getVehicleType());
        assertEquals(210.0, vanFareStrategy.getRatePerKm());
    }

    @Test
    @DisplayName("2. Minimum Fare Rule: Under 1 km distance must return minimum base fare")
    void testMinimumFareForShortTrips() {
        double shortDist = 0.4; // < 1 km

        assertEquals(60.0, bikeFareStrategy.calculateFare(shortDist));
        assertEquals(120.0, threeWheelerFareStrategy.calculateFare(shortDist));
        assertEquals(170.0, carFareStrategy.calculateFare(shortDist));
        assertEquals(210.0, vanFareStrategy.calculateFare(shortDist));
    }

    @Test
    @DisplayName("3. RideFareCalculator Context: Resolves correct strategy by vehicle type (case-insensitive & aliases)")
    void testRideFareCalculatorContext() {
        // Direct type names
        assertEquals(600.0, fareCalculator.calculateFare("Bike", 10.0));
        assertEquals(1200.0, fareCalculator.calculateFare("Three-Wheeler", 10.0));
        assertEquals(1700.0, fareCalculator.calculateFare("Car", 10.0));
        assertEquals(2100.0, fareCalculator.calculateFare("Van", 10.0));

        // Aliases for 3-wheeler
        assertEquals(1200.0, fareCalculator.calculateFare("ThreeWheeler", 10.0));
        assertEquals(1200.0, fareCalculator.calculateFare("Wheel", 10.0));

        // Unknown vehicle type falls back gracefully to default (Car)
        assertEquals(1700.0, fareCalculator.calculateFare("UnknownSpaceShip", 10.0));

        // All fares map
        Map<String, Double> allFares = fareCalculator.calculateAllFares(10.0);
        assertEquals(4, allFares.size());
        assertEquals(600.0, allFares.get("Bike"));
        assertEquals(1200.0, allFares.get("Three-Wheeler"));
        assertEquals(1700.0, allFares.get("Car"));
        assertEquals(2100.0, allFares.get("Van"));
    }

    @Test
    @DisplayName("4. Quote API: Invokes Strategy Pattern in production HTTP request flow")
    void testQuoteFareControllerEndpoint() {
        ResponseEntity<Map<String, Object>> response = bookingController.quoteFare("Car", 15.0, null, null, null, null);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Car", body.get("vehicleType"));
        assertEquals(15.0, body.get("distanceKm"));
        assertEquals(2550.0, body.get("fare")); // 15 * 170 = 2550
    }
}

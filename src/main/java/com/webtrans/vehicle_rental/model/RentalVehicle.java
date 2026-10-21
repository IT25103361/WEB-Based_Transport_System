package com.webtrans.vehicle_rental.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rent_vehicles")
public class RentalVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_name")
    private String name;
    @Column(name = "vehicle_type")
    private String type; // Car, Bike, Van, Luxury
    @Column(name = "daily_rate")
    private Double dailyRate;
    @Column(name = "vehicle_status")
    private String status; // AVAILABLE, RENTED
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    public RentalVehicle() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getDailyRate() { return dailyRate; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String resolveImageUrl(String contextPath) {
        String cp = (contextPath == null || "/".equals(contextPath)) ? "" : contextPath;
        if (imageUrl == null || imageUrl.trim().isEmpty() || imageUrl.contains("google.com/imgres")) {
            String t = (type != null) ? type.trim().toLowerCase() : "car";
            if (!t.equals("car") && !t.equals("van") && !t.equals("bike") && !t.equals("luxury")) {
                t = "vehicle-fallback";
            }
            return cp + "/rental/images/" + t + ".svg";
        }
        return imageUrl.trim();
    }
}

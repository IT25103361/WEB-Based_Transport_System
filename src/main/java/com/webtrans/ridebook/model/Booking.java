package com.webtrans.ridebook.model;

import com.webtrans.auth.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String passengerName;
    private String pickupLocation;
    private String dropLocation;
    private String vehicleType;
    private Double fare;
    private String status;
    private String paymentMethod; // Added to match database column
    private Integer rating;
    private String reviewComment;
    private String appliedOffer;
    private Double finalFare;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    private User passengerUser;
    
    private Double pickupLat;
    private Double pickupLng;
    private Double dropLat;
    private Double dropLng;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    // Default Constructor
    public Booking() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDropLocation() { return dropLocation; }
    public void setDropLocation(String dropLocation) { this.dropLocation = dropLocation; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Double getFare() { return fare; }
    public void setFare(Double fare) { this.fare = fare; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }

    public String getAppliedOffer() { return appliedOffer; }
    public void setAppliedOffer(String appliedOffer) { this.appliedOffer = appliedOffer; }

    public Double getFinalFare() { return finalFare; }
    public void setFinalFare(Double finalFare) { this.finalFare = finalFare; }

    public User getPassengerUser() { return passengerUser; }
    public void setPassengerUser(User passengerUser) { this.passengerUser = passengerUser; }

    public Long getPassengerId() {
        return passengerUser != null ? passengerUser.getId() : null;
    }

    public void setPassengerId(Long passengerId) {
        if (passengerId == null) {
            this.passengerUser = null;
        } else {
            if (this.passengerUser == null || !passengerId.equals(this.passengerUser.getId())) {
                User u = new User();
                u.setId(passengerId);
                this.passengerUser = u;
            }
        }
    }

    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double pickupLat) { this.pickupLat = pickupLat; }

    public Double getPickupLng() { return pickupLng; }
    public void setPickupLng(Double pickupLng) { this.pickupLng = pickupLng; }

    public Double getDropLat() { return dropLat; }
    public void setDropLat(Double dropLat) { this.dropLat = dropLat; }

    public Double getDropLng() { return dropLng; }
    public void setDropLng(Double dropLng) { this.dropLng = dropLng; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }
}
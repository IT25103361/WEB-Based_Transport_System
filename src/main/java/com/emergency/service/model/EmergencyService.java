package com.emergency.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "emergency_services")
public class EmergencyService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Service type is required")
    private String serviceType;

    @NotBlank(message = "Requester name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String requesterName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits"
    )
    private String phoneNumber;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 150, message = "Location must be between 2 and 150 characters")
    private String location;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @NotBlank(message = "Status is required")
    private String status;


    public EmergencyService() {
    }


    public EmergencyService(String serviceType,
                            String requesterName,
                            String phoneNumber,
                            String location,
                            String description,
                            String status) {

        this.serviceType = serviceType;
        this.requesterName = requesterName;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.description = description;
        this.status = status;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }


    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


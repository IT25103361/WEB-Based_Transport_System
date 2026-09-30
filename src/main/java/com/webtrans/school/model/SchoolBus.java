package com.webtrans.school.model;

import jakarta.persistence.*;

@Entity
@Table(name="school_buses")
public class SchoolBus {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Column(unique=true)
    public String registrationNumber;
    public String busName;
    public Long routeId;
    public Integer capacity;
    public String driverName;
    public String driverPhone;
    public String currentStatus;
    public Boolean active;
}
package com.webtrans.school.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "transport_schedules")
public class TransportSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long studentId;
    public Long busId; // primary morning bus id

    public Long morningBusId;
    public String morningBusNumber;
    public String morningBusName;
    public String morningDriver;
    public String morningPhone;

    public Long returnBusId;
    public String returnBusNumber;
    public String returnBusName;
    public String returnDriver;
    public String returnPhone;

    public Long routeId;
    public String routeName;
    public String originCity;
    public String destinationSchool;

    public String pickupStop;
    public String dropOffStop;
    public LocalTime pickupTime;
    public LocalTime expectedArrival;
    public String daysOfOperation;
    public Boolean active;
}
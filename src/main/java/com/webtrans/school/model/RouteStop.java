package com.webtrans.school.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="route_stops")
public class RouteStop {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public Long routeId;
    public String stopName;
    public Double latitude;
    public Double longitude;
    public Integer geofenceRadius;
    public Integer stopSequence;
    public LocalTime pickupTime;
    public LocalTime dropOffTime;
}
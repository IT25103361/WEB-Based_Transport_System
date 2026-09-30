package com.webtrans.school.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="bus_locations")
public class BusLocation {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public Long busId;
    public Double latitude;
    public Double longitude;
    public Double speed;
    public Double heading;
    public LocalDateTime recordedAt;
    public String gpsStatus;
}
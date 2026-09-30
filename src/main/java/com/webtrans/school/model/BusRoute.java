package com.webtrans.school.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bus_routes")
public class BusRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String routeName;
    public String routeDescription;
    public String originCity;
    public String schoolName;
    public String startLocation;
    public String schoolLocation;
    public Boolean active;
}
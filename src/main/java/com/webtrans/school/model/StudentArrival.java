package com.webtrans.school.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="student_arrivals")
public class StudentArrival {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    public Long studentId;
    public Long busId;
    public Long stopId;
    public LocalDateTime arrivalTime;
    public String eventType;
    public Boolean confirmed;
    public LocalDateTime createdAt;
}
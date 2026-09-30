package com.webtrans.school.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="students")
public class Student {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    public Parent parent;

    public String fullName;
    public LocalDate dateOfBirth;
    public String grade;
    public String schoolName;

    @Column(unique=true)
    public String registrationNumber;

    public String transportStatus;
}
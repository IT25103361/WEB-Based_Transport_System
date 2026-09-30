package com.webtrans.school.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="parents")
public class Parent {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public String fullName;
    @Column(unique=true)
    public String email;
    public String phone;
    public String passwordHash;
    public String accountStatus;
    public LocalDateTime createdAt;
}
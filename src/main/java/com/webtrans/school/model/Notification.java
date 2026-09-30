package com.webtrans.school.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public Long parentId;
    public Long studentId;
    public String type;
    public String title;
    @Column(length=1000)
    public String message;
    public LocalDateTime createdAt;
    public Boolean readStatus;
}
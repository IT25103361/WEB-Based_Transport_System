package com.webtrans.emergency.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emergency_requests", indexes = {
        @Index(name = "idx_emergency_tracking_token", columnList = "tracking_token", unique = true),
        @Index(name = "idx_emergency_status", columnList = "status"),
        @Index(name = "idx_emergency_created_at", columnList = "created_at"),
        @Index(name = "idx_emergency_idempotency", columnList = "idempotency_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_token", nullable = false, unique = true, length = 64)
    private String trackingToken;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "requested_services", nullable = false, length = 255)
    private String requestedServices;

    @Column(name = "requested_agent_count")
    private Integer requestedAgentCount;

    @Column(name = "assigned_agents_count", nullable = false)
    @Builder.Default
    private Integer assignedAgentsCount = 0;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_address", length = 500)
    private String locationAddress;

    @Column(name = "location_landmark", length = 255)
    private String locationLandmark;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "callback_phone", nullable = false, length = 30)
    private String callbackPhone;

    @Column(name = "incident_description", nullable = false, length = 1000)
    private String incidentDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private EmergencyStatus status = EmergencyStatus.SUBMITTED;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column(name = "management_token_hash", length = 64)
    private String managementTokenHash;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "bit default 0")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deletion_reason", length = 100)
    private String deletionReason;

    @Column(name = "deletion_note", length = 500)
    private String deletionNote;

    @Column(name = "deleted_by_actor_type", length = 50)
    private String deletedByActorType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "emergencyRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmergencyStatusHistory> statusHistory = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = EmergencyStatus.SUBMITTED;
        if (assignedAgentsCount == null) assignedAgentsCount = 0;
        if (isDeleted == null) isDeleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addStatusHistory(EmergencyStatus newStatus, String note, String changedBy) {
        EmergencyStatusHistory entry = EmergencyStatusHistory.builder()
                .emergencyRequest(this)
                .status(newStatus)
                .note(note)
                .changedBy(changedBy != null ? changedBy : "SYSTEM")
                .createdAt(LocalDateTime.now())
                .build();
        this.statusHistory.add(entry);
    }
}

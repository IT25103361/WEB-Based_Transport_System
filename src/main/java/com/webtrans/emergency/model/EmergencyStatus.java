package com.webtrans.emergency.model;

public enum EmergencyStatus {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    RESPONDERS_ASSIGNED("Responders Assigned"),
    ON_SCENE("On Scene"),
    RESOLVED("Resolved"),
    CANCELLED("Cancelled"),
    DELETED("Deleted");

    private final String displayName;

    EmergencyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

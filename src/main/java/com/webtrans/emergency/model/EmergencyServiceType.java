package com.webtrans.emergency.model;

public enum EmergencyServiceType {
    AMBULANCE("Ambulance", "Medical emergency and patient evacuation", "fa-truck-medical"),
    FIRE_VEHICLE("Fire Vehicle", "Fire control, hazard containment and rescue", "fa-fire-extinguisher"),
    POLICE_ASSISTANCE("Police Assistance", "Security support, traffic and accident containment", "fa-shield-halved"),
    RESPONSE_AGENTS("Emergency Response Agents", "Certified crowd management and incident safety agents", "fa-user-shield");

    private final String title;
    private final String description;
    private final String icon;

    EmergencyServiceType(String title, String description, String icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public static boolean isValid(String name) {
        if (name == null) return false;
        for (EmergencyServiceType type : values()) {
            if (type.name().equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }
}

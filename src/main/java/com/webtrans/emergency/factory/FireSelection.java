package com.webtrans.emergency.factory;

import com.webtrans.emergency.model.EmergencyServiceType;

/**
 * Concrete Product for Fire Vehicle emergency service selection.
 */
public class FireSelection implements EmergencyServiceSelection {

    @Override
    public EmergencyServiceType getServiceType() {
        return EmergencyServiceType.FIRE_VEHICLE;
    }

    @Override
    public String getServiceCode() {
        return EmergencyServiceType.FIRE_VEHICLE.name();
    }

    @Override
    public void validate() {
        // Fire vehicle service selection requires no extra parameters
    }

    @Override
    public Integer getRequestedAgentCount() {
        return null;
    }

    @Override
    public String getSelectionSummary() {
        return "Fire Vehicle - Fire control, hazard containment and rescue";
    }
}

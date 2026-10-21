package com.webtrans.emergency.factory;

import com.webtrans.emergency.model.EmergencyServiceType;

/**
 * Concrete Product for Ambulance emergency service selection.
 */
public class AmbulanceSelection implements EmergencyServiceSelection {

    @Override
    public EmergencyServiceType getServiceType() {
        return EmergencyServiceType.AMBULANCE;
    }

    @Override
    public String getServiceCode() {
        return EmergencyServiceType.AMBULANCE.name();
    }

    @Override
    public void validate() {
        // Ambulance service selection requires no extra parameters
    }

    @Override
    public Integer getRequestedAgentCount() {
        return null;
    }

    @Override
    public String getSelectionSummary() {
        return "Ambulance - Medical emergency and patient evacuation";
    }
}

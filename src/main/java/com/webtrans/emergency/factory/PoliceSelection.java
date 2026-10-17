package com.webtrans.emergency.factory;

import com.webtrans.emergency.model.EmergencyServiceType;

/**
 * Concrete Product for Police Assistance emergency service selection.
 */
public class PoliceSelection implements EmergencyServiceSelection {

    @Override
    public EmergencyServiceType getServiceType() {
        return EmergencyServiceType.POLICE_ASSISTANCE;
    }

    @Override
    public String getServiceCode() {
        return EmergencyServiceType.POLICE_ASSISTANCE.name();
    }

    @Override
    public void validate() {
        // Police assistance service selection requires no extra parameters
    }

    @Override
    public Integer getRequestedAgentCount() {
        return null;
    }

    @Override
    public String getSelectionSummary() {
        return "Police Assistance - Security support, traffic and accident containment";
    }
}

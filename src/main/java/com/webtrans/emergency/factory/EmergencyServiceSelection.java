package com.webtrans.emergency.factory;

import com.webtrans.emergency.model.EmergencyServiceType;

/**
 * Common Product interface for emergency service selections (Factory Pattern).
 * Encapsulates service-specific validation and request preparation behavior.
 */
public interface EmergencyServiceSelection {

    EmergencyServiceType getServiceType();

    String getServiceCode();

    void validate();

    Integer getRequestedAgentCount();

    String getSelectionSummary();
}

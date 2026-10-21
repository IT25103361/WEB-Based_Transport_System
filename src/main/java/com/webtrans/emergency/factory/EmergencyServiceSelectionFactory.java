package com.webtrans.emergency.factory;

/**
 * Simple Factory class for creating EmergencyServiceSelection products.
 * Follows the factory-class approach taught in software design lectures.
 */
public class EmergencyServiceSelectionFactory {

    private EmergencyServiceSelectionFactory() {
        // Prevent direct instantiation of factory utility
    }

    /**
     * Factory method to create appropriate EmergencyServiceSelection product based on service code.
     *
     * @param rawServiceCode The service code or name requested (e.g. AMBULANCE, FIRE_VEHICLE, POLICE_ASSISTANCE, RESPONSE_AGENTS)
     * @param agentCount     The requested agent count (applicable if RESPONSE_AGENTS is chosen)
     * @return Concrete EmergencyServiceSelection product
     * @throws IllegalArgumentException if the service code is invalid or empty
     */
    public static EmergencyServiceSelection createSelection(String rawServiceCode, Integer agentCount) {
        if (rawServiceCode == null || rawServiceCode.isBlank()) {
            throw new IllegalArgumentException("Please select at least one emergency service.");
        }

        String normalized = rawServiceCode.trim().toUpperCase();

        switch (normalized) {
            case "AMBULANCE":
                return new AmbulanceSelection();

            case "FIRE_VEHICLE":
            case "FIRE":
                return new FireSelection();

            case "POLICE_ASSISTANCE":
            case "POLICE":
                return new PoliceSelection();

            case "RESPONSE_AGENTS":
            case "RESPONSE_AGENT":
                return new ResponseAgentsSelection(agentCount);

            default:
                throw new IllegalArgumentException("Invalid service selected: " + rawServiceCode);
        }
    }
}

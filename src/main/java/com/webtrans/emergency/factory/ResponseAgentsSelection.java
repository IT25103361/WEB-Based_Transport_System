package com.webtrans.emergency.factory;

import com.webtrans.emergency.model.EmergencyServiceType;

/**
 * Concrete Product for Emergency Response Agents selection.
 * Enforces agent count validation (1 to 5 inclusive).
 */
public class ResponseAgentsSelection implements EmergencyServiceSelection {

    private final Integer agentCount;

    public ResponseAgentsSelection(Integer agentCount) {
        this.agentCount = agentCount;
    }

    @Override
    public EmergencyServiceType getServiceType() {
        return EmergencyServiceType.RESPONSE_AGENTS;
    }

    @Override
    public String getServiceCode() {
        return EmergencyServiceType.RESPONSE_AGENTS.name();
    }

    @Override
    public void validate() {
        if (agentCount == null) {
            throw new IllegalArgumentException("Please specify the number of Emergency Response Agents (between 1 and 5).");
        }
        if (agentCount < 1 || agentCount > 5) {
            throw new IllegalArgumentException("Emergency Response Agents must be an integer between 1 and 5 inclusive.");
        }
    }

    @Override
    public Integer getRequestedAgentCount() {
        return agentCount;
    }

    @Override
    public String getSelectionSummary() {
        return "Emergency Response Agents - " + (agentCount != null ? agentCount : 0) + " agent(s) requested";
    }
}

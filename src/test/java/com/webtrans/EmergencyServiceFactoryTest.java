package com.webtrans;

import com.webtrans.emergency.factory.*;
import com.webtrans.emergency.model.EmergencyServiceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmergencyServiceFactoryTest {

    @Test
    @DisplayName("1. Simple Factory: Creates correct concrete product for each service code")
    void testFactoryCreatesCorrectProducts() {
        // Ambulance
        EmergencyServiceSelection ambulance = EmergencyServiceSelectionFactory.createSelection("AMBULANCE", null);
        assertNotNull(ambulance);
        assertTrue(ambulance instanceof AmbulanceSelection);
        assertEquals(EmergencyServiceType.AMBULANCE, ambulance.getServiceType());
        assertEquals("AMBULANCE", ambulance.getServiceCode());
        assertNull(ambulance.getRequestedAgentCount());
        assertDoesNotThrow(ambulance::validate);

        // Fire
        EmergencyServiceSelection fire = EmergencyServiceSelectionFactory.createSelection("FIRE_VEHICLE", null);
        assertNotNull(fire);
        assertTrue(fire instanceof FireSelection);
        assertEquals(EmergencyServiceType.FIRE_VEHICLE, fire.getServiceType());
        assertEquals("FIRE_VEHICLE", fire.getServiceCode());
        assertNull(fire.getRequestedAgentCount());
        assertDoesNotThrow(fire::validate);

        // Police
        EmergencyServiceSelection police = EmergencyServiceSelectionFactory.createSelection("POLICE_ASSISTANCE", null);
        assertNotNull(police);
        assertTrue(police instanceof PoliceSelection);
        assertEquals(EmergencyServiceType.POLICE_ASSISTANCE, police.getServiceType());
        assertEquals("POLICE_ASSISTANCE", police.getServiceCode());
        assertNull(police.getRequestedAgentCount());
        assertDoesNotThrow(police::validate);

        // Response Agents
        EmergencyServiceSelection agents = EmergencyServiceSelectionFactory.createSelection("RESPONSE_AGENTS", 3);
        assertNotNull(agents);
        assertTrue(agents instanceof ResponseAgentsSelection);
        assertEquals(EmergencyServiceType.RESPONSE_AGENTS, agents.getServiceType());
        assertEquals("RESPONSE_AGENTS", agents.getServiceCode());
        assertEquals(3, agents.getRequestedAgentCount());
        assertDoesNotThrow(agents::validate);
    }

    @Test
    @DisplayName("2. ResponseAgentsSelection: Agent count 1 and 5 succeed; 0, 6, and negative fail")
    void testAgentCountEnforcementInProduct() {
        // Valid boundary 1
        EmergencyServiceSelection agents1 = EmergencyServiceSelectionFactory.createSelection("RESPONSE_AGENTS", 1);
        assertDoesNotThrow(agents1::validate);

        // Valid boundary 5
        EmergencyServiceSelection agents5 = EmergencyServiceSelectionFactory.createSelection("RESPONSE_AGENTS", 5);
        assertDoesNotThrow(agents5::validate);

        // Invalid: 0
        EmergencyServiceSelection agents0 = EmergencyServiceSelectionFactory.createSelection("RESPONSE_AGENTS", 0);
        IllegalArgumentException ex0 = assertThrows(IllegalArgumentException.class, agents0::validate);
        assertTrue(ex0.getMessage().contains("Emergency Response Agents must be an integer between 1 and 5 inclusive."));

        // Invalid: 6
        EmergencyServiceSelection agents6 = EmergencyServiceSelectionFactory.createSelection("RESPONSE_AGENTS", 6);
        IllegalArgumentException ex6 = assertThrows(IllegalArgumentException.class, agents6::validate);
        assertTrue(ex6.getMessage().contains("Emergency Response Agents must be an integer between 1 and 5 inclusive."));

        // Invalid: negative
        EmergencyServiceSelection agentsNeg = EmergencyServiceSelectionFactory.createSelection("RESPONSE_AGENTS", -2);
        IllegalArgumentException exNeg = assertThrows(IllegalArgumentException.class, agentsNeg::validate);
        assertTrue(exNeg.getMessage().contains("Emergency Response Agents must be an integer between 1 and 5 inclusive."));

        // Invalid: null
        EmergencyServiceSelection agentsNull = EmergencyServiceSelectionFactory.createSelection("RESPONSE_AGENTS", null);
        IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, agentsNull::validate);
        assertTrue(exNull.getMessage().contains("Please specify the number of Emergency Response Agents (between 1 and 5)."));
    }

    @Test
    @DisplayName("3. Factory Validation: Unknown or invalid service codes rejected")
    void testInvalidServiceRejection() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                EmergencyServiceSelectionFactory.createSelection("MILITARY_SUBMARINE", null)
        );
        assertTrue(ex.getMessage().contains("Invalid service selected: MILITARY_SUBMARINE"));
    }
}

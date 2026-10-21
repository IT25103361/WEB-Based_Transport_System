package com.webtrans;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import com.webtrans.emergency.dto.EmergencyAdminUpdateDto;
import com.webtrans.emergency.dto.EmergencyCreateRequestDto;
import com.webtrans.emergency.dto.EmergencyDeleteRequestDto;
import com.webtrans.emergency.dto.EmergencyUpdateRequestDto;
import com.webtrans.emergency.model.EmergencyRequest;
import com.webtrans.emergency.model.EmergencyStatus;
import com.webtrans.emergency.repository.EmergencyRequestRepository;
import com.webtrans.emergency.service.EmergencyCsrfService;
import com.webtrans.emergency.service.EmergencyRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EmergencyServicesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmergencyRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmergencyCsrfService csrfService;

    @Autowired
    private EmergencyRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter.resetForTesting();
    }

    @Test
    @DisplayName("1. Public Access: Unauthenticated guest can open Emergency portal without login")
    void testPublicAccessToEmergencyPortal() throws Exception {
        mockMvc.perform(get("/emergency"))
                .andExpect(status().isOk())
                .andExpect(view().name("emergency/index"))
                .andExpect(model().attributeExists("csrfToken"));
    }

    @Test
    @DisplayName("2. Public Access: Unauthenticated guest can open tracking page without login")
    void testPublicAccessToTrackingPage() throws Exception {
        mockMvc.perform(get("/emergency/track"))
                .andExpect(status().isOk())
                .andExpect(view().name("emergency/track"));
    }

    @Test
    @DisplayName("3. Request Submission: Valid emergency request creates incident with unguessable token and honest demo notice")
    void testSuccessfulEmergencySubmission() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        EmergencyCreateRequestDto dto = EmergencyCreateRequestDto.builder()
                .services(List.of("AMBULANCE", "POLICE_ASSISTANCE"))
                .latitude(6.9271)
                .longitude(79.8612)
                .locationAddress("Colombo Fort Railway Station")
                .callbackPhone("0771234567")
                .contactName("Sunil Perera")
                .incidentDescription("Minor road collision, medical checkup requested.")
                .idempotencyKey(UUID.randomUUID().toString())
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trackingToken", startsWith("EMG-")))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.statusDisplayName").value("Submitted"))
                .andExpect(jsonPath("$.demonstrationMode").value(true))
                .andExpect(jsonPath("$.systemNotice", containsString("Emergency responders have not been contacted")))
                .andExpect(jsonPath("$.requestedServices", hasItems("AMBULANCE", "POLICE_ASSISTANCE")))
                .andExpect(jsonPath("$.locationSummary", containsString("Colombo Fort Railway Station")));
    }

    @Test
    @DisplayName("4. Agent Count Validation: RESPONSE_AGENTS accepts 1 and 5; rejects 0, 6, and negative numbers")
    void testAgentCountEnforcement() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        // Case A: count = 1 (valid)
        EmergencyCreateRequestDto dto1 = EmergencyCreateRequestDto.builder()
                .services(List.of("RESPONSE_AGENTS"))
                .agentCount(1)
                .latitude(6.9271)
                .longitude(79.8612)
                .callbackPhone("0771234567")
                .incidentDescription("Crowd management near town hall.")
                .idempotencyKey(UUID.randomUUID().toString())
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestedAgentCount").value(1));

        // Case B: count = 5 (valid maximum)
        EmergencyCreateRequestDto dto5 = EmergencyCreateRequestDto.builder()
                .services(List.of("RESPONSE_AGENTS"))
                .agentCount(5)
                .latitude(6.9271)
                .longitude(79.8612)
                .callbackPhone("0771234567")
                .incidentDescription("Large crowd assembly requiring safety monitoring.")
                .idempotencyKey(UUID.randomUUID().toString())
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestedAgentCount").value(5));

        // Case C: count = 0 (invalid: rejected)
        EmergencyCreateRequestDto dto0 = EmergencyCreateRequestDto.builder()
                .services(List.of("RESPONSE_AGENTS"))
                .agentCount(0)
                .latitude(6.9271)
                .longitude(79.8612)
                .callbackPhone("0771234567")
                .incidentDescription("Invalid zero agent request.")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Emergency Response Agents must be an integer between 1 and 5")));

        // Case D: count = 6 (invalid: rejected)
        EmergencyCreateRequestDto dto6 = EmergencyCreateRequestDto.builder()
                .services(List.of("RESPONSE_AGENTS"))
                .agentCount(6)
                .latitude(6.9271)
                .longitude(79.8612)
                .callbackPhone("0771234567")
                .incidentDescription("Invalid over-limit agent request.")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto6)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Emergency Response Agents must be an integer between 1 and 5")));

        // Case E: count = -2 (invalid: rejected)
        EmergencyCreateRequestDto dtoNeg = EmergencyCreateRequestDto.builder()
                .services(List.of("RESPONSE_AGENTS"))
                .agentCount(-2)
                .latitude(6.9271)
                .longitude(79.8612)
                .callbackPhone("0771234567")
                .incidentDescription("Invalid negative agent request.")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoNeg)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("5. Idempotency Mechanism: Re-sending with same idempotency key returns existing incident without creating duplicates")
    void testIdempotencyMechanism() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);
        String sharedKey = "idemp-test-fixed-key-" + System.currentTimeMillis();

        EmergencyCreateRequestDto dto = EmergencyCreateRequestDto.builder()
                .services(List.of("FIRE_VEHICLE"))
                .latitude(6.9100)
                .longitude(79.8500)
                .locationAddress("Kollupitiya Junction")
                .callbackPhone("0719876543")
                .incidentDescription("Smoke detected near warehouse electrical box.")
                .idempotencyKey(sharedKey)
                .csrfToken(csrfToken)
                .build();

        // First attempt: creates incident
        String firstResponse = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token1 = objectMapper.readTree(firstResponse).get("trackingToken").asText();

        // Second attempt with identical idempotencyKey: returns same incident without duplicate
        String secondResponse = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token2 = objectMapper.readTree(secondResponse).get("trackingToken").asText();
        assertEquals(token1, token2, "Idempotent submission must return the exact same incident token");
    }

    @Test
    @DisplayName("6. Location & GPS Fallback: Manual address / landmark is accepted when map GPS is unavailable")
    void testManualLocationFallback() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        // Coordinates are null, but manual address and landmark are provided
        EmergencyCreateRequestDto dto = EmergencyCreateRequestDto.builder()
                .services(List.of("AMBULANCE"))
                .latitude(null)
                .longitude(null)
                .locationAddress("High Level Road, Nugegoda")
                .locationLandmark("Opposite Supermarket")
                .callbackPhone("0772233445")
                .incidentDescription("Elderly passenger feeling dizzy, needs roadside rest and check.")
                .idempotencyKey(UUID.randomUUID().toString())
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.locationSummary", containsString("High Level Road, Nugegoda")))
                .andExpect(jsonPath("$.locationSummary", containsString("Opposite Supermarket")));
    }

    @Test
    @DisplayName("7. Public Tracking Security: Tracking by token works; non-existent token returns 404; private phone is not exposed")
    void testPublicTrackingSecurity() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        EmergencyCreateRequestDto dto = EmergencyCreateRequestDto.builder()
                .services(List.of("POLICE_ASSISTANCE"))
                .latitude(6.9200)
                .longitude(79.8600)
                .locationAddress("Kandy Road, Kiribathgoda")
                .callbackPhone("0779988776")
                .incidentDescription("Vehicle breakdown causing obstruction.")
                .idempotencyKey(UUID.randomUUID().toString())
                .csrfToken(csrfToken)
                .build();

        String response = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("trackingToken").asText();

        // Public tracking endpoint returns limited information
        mockMvc.perform(get("/api/emergency/track/" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingToken").value(token))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.callbackPhone").doesNotExist()) // phone NOT exposed to public tracking
                .andExpect(jsonPath("$.demonstrationMode").value(true));

        // Non-existent token returns 404
        mockMvc.perform(get("/api/emergency/track/EMG-NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("No emergency incident found")));
    }

    @Test
    @DisplayName("8. Operational Console Security: Anonymous user cannot access admin console or update status")
    void testOperationalConsoleAdminSecurity() throws Exception {
        // Anonymous request to admin data endpoint returns 401 Unauthorized
        mockMvc.perform(get("/api/emergency/admin/requests"))
                .andExpect(status().isUnauthorized());

        // Anonymous request to admin page redirects to admin login
        mockMvc.perform(get("/emergency/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login?error=auth_required"));

        // Authenticated Admin user can access operational endpoints
        User adminUser = new User();
        adminUser.setEmail("emg_admin_" + System.currentTimeMillis() + "@nextgo.com");
        adminUser.setFullName("Emergency Dispatch Officer");
        adminUser.setPassword("pass123");
        adminUser.setRole("ADMIN");
        adminUser = userRepository.save(adminUser);

        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("adminUser", adminUser);
        adminSession.setAttribute("adminRole", "ADMIN");

        mockMvc.perform(get("/api/emergency/admin/requests").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)));

        // Create a test request to update
        EmergencyRequest req = EmergencyRequest.builder()
                .trackingToken("EMG-OP-" + System.currentTimeMillis())
                .requestedServices("AMBULANCE")
                .callbackPhone("0771122334")
                .incidentDescription("Test operational update")
                .status(EmergencyStatus.SUBMITTED)
                .build();
        req = requestRepository.save(req);

        // Admin updates status to UNDER_REVIEW with 2 assigned agents
        EmergencyAdminUpdateDto updateDto = EmergencyAdminUpdateDto.builder()
                .status("UNDER_REVIEW")
                .assignedAgents(2)
                .note("Assigned local response unit for triage assessment.")
                .build();

        mockMvc.perform(post("/api/emergency/admin/requests/" + req.getId() + "/status")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.assignedAgentsCount").value(2));
    }

    @Test
    @DisplayName("9. Module Isolation: Unrelated protected modules still require authentication while Emergency remains public")
    void testModuleIsolationAndNonEmergencyProtection() throws Exception {
        // Unauthenticated passenger portal access redirects to passenger login
        mockMvc.perform(get("/ride/passenger"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ride/passenger/login"));

        // Unauthenticated driver portal access redirects to driver login
        mockMvc.perform(get("/ride/driver"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ride/driver/login"));

        // Public Home page opens without login
        mockMvc.perform(get("/ride/home"))
                .andExpect(status().isOk());

        // Emergency module opens without login
        mockMvc.perform(get("/emergency"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("10. Background Asset: Emergency background image loads without 404 for unauthenticated guests")
    void testEmergencyBackgroundImageLoadsWithout404() throws Exception {
        mockMvc.perform(get("/emergency/images/emergency-background.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    @DisplayName("11. Requester Deletion: Authorized requester can delete eligible request with valid reason and audit details stored")
    void testAuthorizedRequesterCanDeleteRequestWithValidReason() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        EmergencyCreateRequestDto createDto = EmergencyCreateRequestDto.builder()
                .services(List.of("AMBULANCE"))
                .locationAddress("Marine Drive, Bambalapitiya")
                .callbackPhone("0775566778")
                .incidentDescription("Minor injury, patient attended by clinic nearby.")
                .idempotencyKey(UUID.randomUUID().toString())
                .csrfToken(csrfToken)
                .build();

        String createRes = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.managementToken", startsWith("MGT-")))
                .andReturn().getResponse().getContentAsString();

        String trackingToken = objectMapper.readTree(createRes).get("trackingToken").asText();
        String managementToken = objectMapper.readTree(createRes).get("managementToken").asText();

        // Perform deletion with mandatory reason
        EmergencyDeleteRequestDto deleteDto = EmergencyDeleteRequestDto.builder()
                .trackingToken(trackingToken)
                .managementToken(managementToken)
                .reason("Assistance no longer required")
                .note("Private transport arranged to hospital.")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message", containsString("successfully deleted")));

        // Verify soft-deletion and audit records in DB
        EmergencyRequest dbRecord = requestRepository.findByTrackingToken(trackingToken).orElseThrow();
        assertTrue(dbRecord.getIsDeleted(), "Incident must be marked soft-deleted");
        assertNotNull(dbRecord.getDeletedAt(), "deletedAt timestamp must be recorded");
        assertEquals("Assistance no longer required", dbRecord.getDeletionReason());
        assertEquals("Private transport arranged to hospital.", dbRecord.getDeletionNote());
        assertEquals("REQUESTER", dbRecord.getDeletedByActorType());
        assertEquals(EmergencyStatus.DELETED, dbRecord.getStatus());
    }

    @Test
    @DisplayName("12. Deletion Validation: Missing reason is rejected with 400 Bad Request")
    void testMissingDeletionReasonRejected() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        EmergencyCreateRequestDto createDto = EmergencyCreateRequestDto.builder()
                .services(List.of("POLICE_ASSISTANCE"))
                .locationAddress("Duplication Road")
                .callbackPhone("0773344556")
                .incidentDescription("Traffic stall incident.")
                .csrfToken(csrfToken)
                .build();

        String createRes = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String trackingToken = objectMapper.readTree(createRes).get("trackingToken").asText();
        String managementToken = objectMapper.readTree(createRes).get("managementToken").asText();

        // Missing reason
        EmergencyDeleteRequestDto deleteDto = EmergencyDeleteRequestDto.builder()
                .trackingToken(trackingToken)
                .managementToken(managementToken)
                .reason("") // empty reason
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("deletion reason is mandatory")));
    }

    @Test
    @DisplayName("13. Deletion Validation: 'Other' reason requires explanation with >= 5 characters")
    void testOtherReasonRequiresExplanationAtLeastFiveChars() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        EmergencyCreateRequestDto createDto = EmergencyCreateRequestDto.builder()
                .services(List.of("FIRE_VEHICLE"))
                .locationAddress("Union Place")
                .callbackPhone("0712233445")
                .incidentDescription("False alarm triggered by incense.")
                .csrfToken(csrfToken)
                .build();

        String createRes = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String trackingToken = objectMapper.readTree(createRes).get("trackingToken").asText();
        String managementToken = objectMapper.readTree(createRes).get("managementToken").asText();

        // Case A: 'Other' with no explanation
        EmergencyDeleteRequestDto deleteDtoNoNote = EmergencyDeleteRequestDto.builder()
                .trackingToken(trackingToken)
                .managementToken(managementToken)
                .reason("Other")
                .note(null)
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDtoNoNote)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("at least 5 characters")));

        // Case B: 'Other' with short explanation (< 5 chars)
        EmergencyDeleteRequestDto deleteDtoShortNote = EmergencyDeleteRequestDto.builder()
                .trackingToken(trackingToken)
                .managementToken(managementToken)
                .reason("Other")
                .note("test")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDtoShortNote)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("at least 5 characters")));

        // Case C: 'Other' with valid explanation (>= 5 chars) succeeds
        EmergencyDeleteRequestDto deleteDtoValid = EmergencyDeleteRequestDto.builder()
                .trackingToken(trackingToken)
                .managementToken(managementToken)
                .reason("Other")
                .note("Accidental trigger caused by commercial kitchen steam.")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDtoValid)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("14. Authorization: Tracking-only visitor and incorrect management token are rejected with 403 Forbidden")
    void testUnauthorizedTrackingVisitorCannotDelete() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        EmergencyCreateRequestDto createDto = EmergencyCreateRequestDto.builder()
                .services(List.of("AMBULANCE"))
                .locationAddress("Galle Face Terrace")
                .callbackPhone("0778899001")
                .incidentDescription("Test authorization isolation incident.")
                .csrfToken(csrfToken)
                .build();

        String createRes = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String trackingToken = objectMapper.readTree(createRes).get("trackingToken").asText();

        // Case A: Missing management token (e.g. someone using only the tracking URL)
        EmergencyDeleteRequestDto noTokenDto = EmergencyDeleteRequestDto.builder()
                .trackingToken(trackingToken)
                .managementToken("")
                .reason("Created by mistake")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noTokenDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("Management authorization token is required")));

        // Case B: Forged / guessed management token
        EmergencyDeleteRequestDto wrongTokenDto = EmergencyDeleteRequestDto.builder()
                .trackingToken(trackingToken)
                .managementToken("MGT-GUESSEDTOKEN1234567890ABCDEF")
                .reason("Created by mistake")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongTokenDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("Invalid management authorization token")));
    }

    @Test
    @DisplayName("15. Status Lifecycle: Deletion is blocked if responders are already assigned or operational status has advanced")
    void testCannotDeleteAcceptedOrDispatchedIncident() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        EmergencyCreateRequestDto createDto = EmergencyCreateRequestDto.builder()
                .services(List.of("RESPONSE_AGENTS"))
                .agentCount(3)
                .locationAddress("Independence Square")
                .callbackPhone("0774455667")
                .incidentDescription("Security support incident for active event.")
                .csrfToken(csrfToken)
                .build();

        String createRes = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String trackingToken = objectMapper.readTree(createRes).get("trackingToken").asText();
        String managementToken = objectMapper.readTree(createRes).get("managementToken").asText();

        // Simulate admin dispatching units
        EmergencyRequest req = requestRepository.findByTrackingToken(trackingToken).orElseThrow();
        req.setStatus(EmergencyStatus.RESPONDERS_ASSIGNED);
        req.setAssignedAgentsCount(2);
        requestRepository.save(req);

        // Requester tries to delete
        EmergencyDeleteRequestDto deleteDto = EmergencyDeleteRequestDto.builder()
                .trackingToken(trackingToken)
                .managementToken(managementToken)
                .reason("Created by mistake")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("assigned or dispatched")));
    }

    @Test
    @DisplayName("16. Repeated Deletion: Second deletion attempt on an already deleted request is rejected")
    void testRepeatedDeletionAttemptsBlocked() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        EmergencyCreateRequestDto createDto = EmergencyCreateRequestDto.builder()
                .services(List.of("AMBULANCE"))
                .locationAddress("Baseline Road, Dematagoda")
                .callbackPhone("0771122339")
                .incidentDescription("Minor sprain incident.")
                .csrfToken(csrfToken)
                .build();

        String createRes = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String trackingToken = objectMapper.readTree(createRes).get("trackingToken").asText();
        String managementToken = objectMapper.readTree(createRes).get("managementToken").asText();

        EmergencyDeleteRequestDto deleteDto = EmergencyDeleteRequestDto.builder()
                .trackingToken(trackingToken)
                .managementToken(managementToken)
                .reason("Duplicate request")
                .csrfToken(csrfToken)
                .build();

        // First deletion: succeeds
        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isOk());

        // Second deletion: blocked with conflict
        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("already been deleted")));
    }

    @Test
    @DisplayName("17. Active vs Audit Results: Deleted requests disappear from active results while audit information is preserved")
    void testDeletedRequestsExcludedFromActiveResults() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        // Create Incident A (to be deleted)
        EmergencyCreateRequestDto dtoA = EmergencyCreateRequestDto.builder()
                .services(List.of("AMBULANCE"))
                .locationAddress("Incident A Location")
                .callbackPhone("0770000001")
                .incidentDescription("Incident A test.")
                .csrfToken(csrfToken)
                .build();

        String resA = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String tokenA = objectMapper.readTree(resA).get("trackingToken").asText();
        String mgmtA = objectMapper.readTree(resA).get("managementToken").asText();

        // Create Incident B (remains active)
        EmergencyCreateRequestDto dtoB = EmergencyCreateRequestDto.builder()
                .services(List.of("FIRE_VEHICLE"))
                .locationAddress("Incident B Location")
                .callbackPhone("0770000002")
                .incidentDescription("Incident B test.")
                .csrfToken(csrfToken)
                .build();

        String resB = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String tokenB = objectMapper.readTree(resB).get("trackingToken").asText();

        // Delete Incident A
        EmergencyDeleteRequestDto delDto = EmergencyDeleteRequestDto.builder()
                .trackingToken(tokenA)
                .managementToken(mgmtA)
                .reason("Created by mistake")
                .note("Audit preservation test")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(delDto)))
                .andExpect(status().isOk());

        // Authenticate as Admin
        User adminUser = new User();
        adminUser.setEmail("audit_admin_" + System.currentTimeMillis() + "@nextgo.com");
        adminUser.setFullName("Audit Officer");
        adminUser.setPassword("pass123");
        adminUser.setRole("ADMIN");
        adminUser = userRepository.save(adminUser);

        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("adminUser", adminUser);
        adminSession.setAttribute("adminRole", "ADMIN");

        // Active requests: tokenB present, tokenA excluded
        mockMvc.perform(get("/api/emergency/admin/requests").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].trackingToken", not(hasItem(tokenA))))
                .andExpect(jsonPath("$[*].trackingToken", hasItem(tokenB)));

        // Audit requests (includeDeleted=true): tokenA IS present with audit info
        mockMvc.perform(get("/api/emergency/admin/requests?includeDeleted=true").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].trackingToken", hasItem(tokenA)))
                .andExpect(jsonPath("$[?(@.trackingToken == '" + tokenA + "')].isDeleted", contains(true)))
                .andExpect(jsonPath("$[?(@.trackingToken == '" + tokenA + "')].deletionReason", contains("Created by mistake")))
                .andExpect(jsonPath("$[?(@.trackingToken == '" + tokenA + "')].deletionNote", contains("Audit preservation test")))
                .andExpect(jsonPath("$[?(@.trackingToken == '" + tokenA + "')].deletedByActorType", contains("REQUESTER")));
    }

    @Test
    @DisplayName("18. Public Tracking of Deleted Incident: Shows minimal notice without exposing private details")
    void testPublicTrackingOfDeletedIncidentMinimalNotice() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        EmergencyCreateRequestDto createDto = EmergencyCreateRequestDto.builder()
                .services(List.of("AMBULANCE", "POLICE_ASSISTANCE"))
                .locationAddress("Private Home 123, Cinnamon Gardens")
                .callbackPhone("0779900112")
                .contactName("Private Citizen Name")
                .incidentDescription("Very confidential health details.")
                .csrfToken(csrfToken)
                .build();

        String createRes = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String trackingToken = objectMapper.readTree(createRes).get("trackingToken").asText();
        String managementToken = objectMapper.readTree(createRes).get("managementToken").asText();

        // Delete incident
        EmergencyDeleteRequestDto deleteDto = EmergencyDeleteRequestDto.builder()
                .trackingToken(trackingToken)
                .managementToken(managementToken)
                .reason("Incorrect information")
                .note("Wrong location entered")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isOk());

        // Public tracking endpoint check
        mockMvc.perform(get("/api/emergency/track/" + trackingToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingToken").value(trackingToken))
                .andExpect(jsonPath("$.status").value("DELETED"))
                .andExpect(jsonPath("$.isDeleted").value(true))
                .andExpect(jsonPath("$.canDelete").value(false))
                .andExpect(jsonPath("$.systemNotice", containsString("This request is no longer available")))
                .andExpect(jsonPath("$.locationSummary").doesNotExist())
                .andExpect(jsonPath("$.incidentDescription").doesNotExist())
                .andExpect(jsonPath("$.contactName").doesNotExist())
                .andExpect(jsonPath("$.callbackPhone").doesNotExist())
                .andExpect(jsonPath("$.managementToken").doesNotExist())
                .andExpect(jsonPath("$.deletionReason").doesNotExist()) // Reason not exposed in public tracking
                .andExpect(jsonPath("$.deletionNote").doesNotExist());
    }

    @Test
    @DisplayName("19. Idempotency on Deleted Incident: Retrying old submission does not recreate a deleted incident")
    void testRetryingDeletedSubmissionDoesNotRecreateIncident() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);
        String sharedKey = "idemp-prevent-recreate-" + System.currentTimeMillis();

        EmergencyCreateRequestDto createDto = EmergencyCreateRequestDto.builder()
                .services(List.of("AMBULANCE"))
                .locationAddress("Test Address")
                .callbackPhone("0771234567")
                .incidentDescription("Test idempotency on deletion.")
                .idempotencyKey(sharedKey)
                .csrfToken(csrfToken)
                .build();

        String createRes = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String trackingToken = objectMapper.readTree(createRes).get("trackingToken").asText();
        String managementToken = objectMapper.readTree(createRes).get("managementToken").asText();

        // Delete incident
        EmergencyDeleteRequestDto deleteDto = EmergencyDeleteRequestDto.builder()
                .trackingToken(trackingToken)
                .managementToken(managementToken)
                .reason("Created by mistake")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isOk());

        // Resubmit with same idempotencyKey: must return deleted state, NOT recreate
        mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trackingToken").value(trackingToken))
                .andExpect(jsonPath("$.isDeleted").value(true))
                .andExpect(jsonPath("$.status").value("DELETED"));

        // Verify in DB that it is still deleted
        EmergencyRequest dbRecord = requestRepository.findByTrackingToken(trackingToken).orElseThrow();
        assertTrue(dbRecord.getIsDeleted());
    }

    @Test
    @DisplayName("20. Emergency Theme: Pages contain theme toggle markup and dedicated localStorage key script")
    void testEmergencyThemeMarkupAndDedicatedKey() throws Exception {
        // Portal page
        mockMvc.perform(get("/emergency"))
                .andExpect(status().isOk())
                .andExpect(view().name("emergency/index"));

        // Tracking page
        mockMvc.perform(get("/emergency/track"))
                .andExpect(status().isOk())
                .andExpect(view().name("emergency/track"));

        // Assert JSP templates contain theme toggle, dedicated key, and scoped CSS
        java.nio.file.Path indexPath = java.nio.file.Path.of("src/main/webapp/WEB-INF/jsp/emergency/index.jsp");
        String indexContent = java.nio.file.Files.readString(indexPath);
        assertTrue(indexContent.contains("data-emergency-theme-toggle"), "index.jsp must contain theme toggle attribute");
        assertTrue(indexContent.contains("nextgo.emergency.theme"), "index.jsp must contain dedicated emergency theme key");
        assertTrue(indexContent.contains("emergency.css"), "index.jsp must include emergency.css");

        java.nio.file.Path trackPath = java.nio.file.Path.of("src/main/webapp/WEB-INF/jsp/emergency/track.jsp");
        String trackContent = java.nio.file.Files.readString(trackPath);
        assertTrue(trackContent.contains("data-emergency-theme-toggle"), "track.jsp must contain theme toggle attribute");
        assertTrue(trackContent.contains("nextgo.emergency.theme"), "track.jsp must contain dedicated emergency theme key");
        assertTrue(trackContent.contains("emergency.css"), "track.jsp must include emergency.css");
    }

    @Test
    @DisplayName("21. Requester Update: Successfully update location and description of emergency incident report")
    void testSuccessfulEmergencyReportUpdate() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        // Create initial incident
        EmergencyCreateRequestDto createDto = EmergencyCreateRequestDto.builder()
                .services(List.of("FIRE_VEHICLE"))
                .locationAddress("Initial Road, Colombo 03")
                .callbackPhone("0719876543")
                .incidentDescription("Initial fire outbreak description.")
                .csrfToken(csrfToken)
                .build();

        String createResp = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(createResp).get("trackingToken").asText();

        // Perform update for location and description
        EmergencyUpdateRequestDto updateDto = EmergencyUpdateRequestDto.builder()
                .trackingToken(token)
                .locationAddress("Updated Galle Road, Kollupitiya, Colombo 03")
                .locationLandmark("Opposite Liberty Plaza")
                .incidentDescription("Updated situation: Fire has spread to adjacent store, units urgently required.")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/update")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.incident.locationSummary", containsString("Updated Galle Road, Kollupitiya, Colombo 03")))
                .andExpect(jsonPath("$.incident.incidentDescription", containsString("Updated situation: Fire has spread")));

        // Verify database persistence
        EmergencyRequest updatedEntity = requestRepository.findByTrackingToken(token).orElseThrow();
        assertEquals("Updated Galle Road, Kollupitiya, Colombo 03", updatedEntity.getLocationAddress());
        assertEquals("Opposite Liberty Plaza", updatedEntity.getLocationLandmark());
        assertEquals("Updated situation: Fire has spread to adjacent store, units urgently required.", updatedEntity.getIncidentDescription());
    }

    @Test
    @DisplayName("22. Requester Update: Validation rejects empty address or description under 5 chars")
    void testEmergencyReportUpdateValidation() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        EmergencyCreateRequestDto createDto = EmergencyCreateRequestDto.builder()
                .services(List.of("AMBULANCE"))
                .locationAddress("Galle Road, Colombo")
                .callbackPhone("0771234567")
                .incidentDescription("Medical emergency report.")
                .csrfToken(csrfToken)
                .build();

        String createResp = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(createResp).get("trackingToken").asText();

        // 1. Empty location address
        EmergencyUpdateRequestDto emptyAddrDto = EmergencyUpdateRequestDto.builder()
                .trackingToken(token)
                .locationAddress("")
                .incidentDescription("Valid updated description here.")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/update")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyAddrDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Location address is required")));

        // 2. Short description (< 5 chars)
        EmergencyUpdateRequestDto shortDescDto = EmergencyUpdateRequestDto.builder()
                .trackingToken(token)
                .locationAddress("Valid Updated Address")
                .incidentDescription("Bad")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/update")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortDescDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Incident description must be at least 5 characters long")));
    }

    @Test
    @DisplayName("23. Requester Delete: Successfully removes incident report and marks it DELETED")
    void testSuccessfulEmergencyReportDelete() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrfToken = csrfService.getOrCreateToken(session);

        EmergencyCreateRequestDto createDto = EmergencyCreateRequestDto.builder()
                .services(List.of("POLICE_ASSISTANCE"))
                .locationAddress("Bambalapitiya Junction")
                .callbackPhone("0751234567")
                .incidentDescription("Traffic incident needing assistance.")
                .csrfToken(csrfToken)
                .build();

        String createResp = mockMvc.perform(post("/api/emergency/request")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(createResp).get("trackingToken").asText();
        String managementToken = objectMapper.readTree(createResp).get("managementToken").asText();

        EmergencyDeleteRequestDto deleteDto = EmergencyDeleteRequestDto.builder()
                .trackingToken(token)
                .managementToken(managementToken)
                .reason("Assistance no longer required")
                .note("Issue resolved peacefully on site.")
                .csrfToken(csrfToken)
                .build();

        mockMvc.perform(post("/api/emergency/request/delete")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.incident.status").value("DELETED"));

        EmergencyRequest deletedEntity = requestRepository.findByTrackingToken(token).orElseThrow();
        assertTrue(deletedEntity.getIsDeleted());
        assertEquals(EmergencyStatus.DELETED, deletedEntity.getStatus());
        assertEquals("Assistance no longer required", deletedEntity.getDeletionReason());
    }

    @Test
    @DisplayName("24. UI Template Check: track.jsp contains update and delete options and modals")
    void testTrackJspUpdateAndDeleteMarkup() throws Exception {
        java.nio.file.Path trackPath = java.nio.file.Path.of("src/main/webapp/WEB-INF/jsp/emergency/track.jsp");
        String trackContent = java.nio.file.Files.readString(trackPath);
        assertTrue(trackContent.contains("id=\"trackUpdateBtn\""), "track.jsp must contain Update Report button");
        assertTrue(trackContent.contains("id=\"trackDeleteBtn\""), "track.jsp must contain Delete Report button");
        assertTrue(trackContent.contains("id=\"updateIncidentModal\""), "track.jsp must contain update incident modal");
        assertTrue(trackContent.contains("id=\"deletionConfirmModal\""), "track.jsp must contain deletion modal");
    }
}

package com.webtrans;

import com.webtrans.school.model.Notification;
import com.webtrans.school.observer.SchoolTransportEvent;
import com.webtrans.school.observer.SchoolTransportEventPublisher;
import com.webtrans.school.observer.SchoolTransportObserver;
import com.webtrans.school.repository.NotificationRepository;
import com.webtrans.school.service.TransportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SchoolTransportObserverTest {

    @Autowired
    private SchoolTransportEventPublisher eventPublisher;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TransportService transportService;

    @Autowired
    private com.webtrans.school.controller.ApiController apiController;

    @Autowired
    private com.webtrans.school.repository.StudentRepository studentRepository;

    @Test
    @DisplayName("1. Event Publisher & ParentNotificationObserver: Dispatches event and saves scoped notification")
    void testEventPublishingAndNotificationCreation() {
        Long testParentId = 999111L;
        Long testStudentId = 888222L;

        SchoolTransportEvent event = new SchoolTransportEvent(
                "BUS_AT_PICKUP",
                testParentId,
                testStudentId,
                "Bus Arrived at Pickup",
                "Bus NB-1234 has arrived at Kottawa junction."
        );

        eventPublisher.publishEvent(event);

        List<Notification> parentNotifications = notificationRepository.findByParentIdOrderByCreatedAtDesc(testParentId);
        assertNotNull(parentNotifications);
        assertFalse(parentNotifications.isEmpty());

        Notification saved = parentNotifications.get(0);
        assertEquals(testParentId, saved.parentId);
        assertEquals(testStudentId, saved.studentId);
        assertEquals("BUS_AT_PICKUP", saved.type);
        assertEquals("Bus Arrived at Pickup", saved.title);
        assertEquals("Bus NB-1234 has arrived at Kottawa junction.", saved.message);
        assertFalse(saved.readStatus);
    }

    @Test
    @DisplayName("2. Scoped Notification: Notifies ONLY the targeted parent, not unrelated parents")
    void testTargetedParentScoping() {
        Long parentA = 777111L;
        Long parentB = 777222L;

        SchoolTransportEvent event = new SchoolTransportEvent(
                "SCHEDULE_UPDATED",
                parentA,
                101L,
                "Route Assigned",
                "New route assigned for your student."
        );

        eventPublisher.publishEvent(event);

        List<Notification> listA = notificationRepository.findByParentIdOrderByCreatedAtDesc(parentA);
        List<Notification> listB = notificationRepository.findByParentIdOrderByCreatedAtDesc(parentB);

        assertEquals(1, listA.size());
        assertEquals(0, listB.size(), "Unrelated parent B must receive 0 notifications");
    }

    @Test
    @DisplayName("3. TransportService notify delegation: Uses Observer Pattern under the hood")
    void testTransportServiceNotifyDelegation() {
        Long testParentId = 555111L;

        transportService.notify(testParentId, 303L, "SCHEDULE_DELETED", "Assignment Cancelled", "Schedule removed.");

        List<Notification> results = notificationRepository.findByParentIdOrderByCreatedAtDesc(testParentId);
        assertEquals(1, results.size());
        assertEquals("SCHEDULE_DELETED", results.get(0).type);
    }

    @Test
    @DisplayName("4. Dynamic Observer Registration: Subject notifies newly attached observers")
    void testDynamicObserverRegistration() {
        List<SchoolTransportEvent> receivedEvents = new ArrayList<>();
        SchoolTransportObserver customObserver = receivedEvents::add;

        eventPublisher.registerObserver(customObserver);
        try {
            SchoolTransportEvent event = new SchoolTransportEvent(
                    "DEMO_EVENT", 123L, 456L, "Test Event", "Custom observer test."
            );
            eventPublisher.publishEvent(event);

            assertEquals(1, receivedEvents.size());
            assertEquals("DEMO_EVENT", receivedEvents.get(0).getEventType());
        } finally {
            eventPublisher.unregisterObserver(customObserver);
        }
    }

    @Test
    @DisplayName("5. School Transport Payment: Calculates monthly payment at 250 LKR per km (1KM * 250LKR = 250 LKR)")
    void testMonthlyPaymentCalculation() {
        com.webtrans.school.model.Student st = new com.webtrans.school.model.Student();
        st.fullName = "Emily Perera";
        st.schoolName = "Nalanda College";
        st = studentRepository.save(st);

        // Case A: Default 1 KM (matching '1KM * rate per km')
        java.util.Map<String, Object> payment1Km = apiController.payment(st.id, null);
        assertEquals(250.0, payment1Km.get("ratePerKm"));
        assertEquals(1.0, payment1Km.get("distanceKm"));
        assertEquals(250.0, payment1Km.get("monthlyAmount"));
        assertEquals("LKR", payment1Km.get("currency"));
        assertEquals("Monthly", payment1Km.get("billingCycle"));

        // Case B: Route Distance (e.g. 10 KM => 2500 LKR)
        java.util.Map<String, Object> payment10Km = apiController.payment(st.id, 10.0);
        assertEquals(10.0, payment10Km.get("distanceKm"));
        assertEquals(2500.0, payment10Km.get("monthlyAmount"));
    }
}

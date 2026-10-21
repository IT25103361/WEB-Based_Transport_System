package com.webtrans.school.observer;

import com.webtrans.school.model.Notification;
import com.webtrans.school.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Concrete Observer that handles notifying the verified parent of a student.
 * Creates and persists the school notification in the active transaction.
 */
@Component
public class ParentNotificationObserver implements SchoolTransportObserver {

    private final NotificationRepository notifications;

    @Autowired
    public ParentNotificationObserver(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Override
    public void onTransportEvent(SchoolTransportEvent event) {
        if (event == null || event.getParentId() == null) {
            return;
        }

        Notification n = new Notification();
        n.parentId = event.getParentId();
        n.studentId = event.getStudentId();
        n.type = event.getEventType();
        n.title = event.getTitle();
        n.message = event.getMessage();
        n.createdAt = event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now();
        n.readStatus = Boolean.FALSE;

        notifications.save(n);
    }
}

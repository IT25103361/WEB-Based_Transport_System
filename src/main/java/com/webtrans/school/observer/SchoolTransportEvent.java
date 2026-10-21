package com.webtrans.school.observer;

import java.time.LocalDateTime;

/**
 * Immutable event representing a school transport occurrence.
 * Carries all necessary context for observers while preventing unintended mutation.
 */
public final class SchoolTransportEvent {

    private final String eventType;
    private final Long parentId;
    private final Long studentId;
    private final String title;
    private final String message;
    private final LocalDateTime timestamp;

    public SchoolTransportEvent(String eventType, Long parentId, Long studentId, String title, String message) {
        this.eventType = eventType;
        this.parentId = parentId;
        this.studentId = studentId;
        this.title = title;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventType() {
        return eventType;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "SchoolTransportEvent{" +
                "eventType='" + eventType + '\'' +
                ", parentId=" + parentId +
                ", studentId=" + studentId +
                ", title='" + title + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

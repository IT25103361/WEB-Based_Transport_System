package com.webtrans.Courier.service.notification;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The Factory — the heart of the Factory Pattern in this project.
 *
 * Callers ask for a NotificationChannel by type and receive a working object.
 * They never see concrete implementations — only the interface.
 */
@Component
public class NotificationFactory {

    private final Map<NotificationType, NotificationChannel> channels;

    /**
     * Spring injects every NotificationChannel bean. We index them by type
     * so lookup is O(1) at runtime — no if-else, no reflection.
     */
    public NotificationFactory(List<NotificationChannel> channels) {
        this.channels = new EnumMap<>(NotificationType.class);
        for (NotificationChannel c : channels) {
            this.channels.put(c.getType(), c);
        }
    }

    /** Return the correct channel for the given type. */
    public NotificationChannel create(NotificationType type) {
        NotificationChannel channel = channels.get(type);
        if (channel == null) {
            throw new IllegalArgumentException(
                    "No notification channel registered for: " + type);
        }
        return channel;
    }

    /** Convenience: look up + send in one call. */
    public void send(NotificationType type, NotificationRequest request) {
        create(type).send(request);
    }
}
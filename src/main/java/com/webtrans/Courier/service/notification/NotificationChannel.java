package com.webtrans.Courier.service.notification;

/**
 * Common contract for every notification channel.
 * The Factory returns implementations of this interface — never concrete classes.
 */
public interface NotificationChannel {

    /** Which type this channel handles. */
    NotificationType getType();

    /** Send the notification. Implementations must never throw. */
    void send(NotificationRequest request);
}
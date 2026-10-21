package com.webtrans.school.observer;

/**
 * Common Observer interface for listening to school transport events.
 */
public interface SchoolTransportObserver {

    void onTransportEvent(SchoolTransportEvent event);
}

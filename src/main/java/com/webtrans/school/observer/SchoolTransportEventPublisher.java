package com.webtrans.school.observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject/Publisher class for School Transport events.
 * Manages observers and notifies them when transport events occur.
 */
@Component
public class SchoolTransportEventPublisher {

    private final List<SchoolTransportObserver> observers = new CopyOnWriteArrayList<>();

    @Autowired
    public SchoolTransportEventPublisher(List<SchoolTransportObserver> initialObservers) {
        if (initialObservers != null) {
            observers.addAll(initialObservers);
        }
    }

    public void registerObserver(SchoolTransportObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unregisterObserver(SchoolTransportObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    public List<SchoolTransportObserver> getObservers() {
        return List.copyOf(observers);
    }

    public void publishEvent(SchoolTransportEvent event) {
        if (event == null) return;
        for (SchoolTransportObserver observer : observers) {
            try {
                observer.onTransportEvent(event);
            } catch (Exception e) {
                // Protect subsequent observers from individual listener errors
                e.printStackTrace();
            }
        }
    }
}

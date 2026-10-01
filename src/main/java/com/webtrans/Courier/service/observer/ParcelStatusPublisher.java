package com.webtrans.Courier.service.observer;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Subject / Publisher in the Observer Pattern.
 *
 * Receives every ParcelStatusObserver bean via Spring's constructor injection,
 * and broadcasts events to them.
 */
@Service
public class ParcelStatusPublisher {

    private final List<ParcelStatusObserver> observers;

    public ParcelStatusPublisher(List<ParcelStatusObserver> observers) {
        this.observers = observers;
    }

    /**
     * Broadcast to every observer. Each observer handles its own concern.
     * Any exception thrown by an observer is logged but does not stop the others.
     */
    public void publish(ParcelStatusEvent event) {
        for (ParcelStatusObserver observer : observers) {
            try {
                observer.onStatusChange(event);
            } catch (Exception e) {
                System.err.println("Observer " + observer.getClass().getSimpleName()
                        + " failed: " + e.getMessage());
            }
        }
    }
}
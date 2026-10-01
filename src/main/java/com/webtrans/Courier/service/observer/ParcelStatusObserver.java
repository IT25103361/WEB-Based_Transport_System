package com.webtrans.Courier.service.observer;

/**
 * Observer interface — every observer that reacts to parcel status changes
 * implements this contract.
 *
 * Spring injects all implementations into ParcelStatusPublisher automatically.
 */
public interface ParcelStatusObserver {

    /**
     * Called by the publisher whenever a parcel's status changes.
     * Implementations must handle their own concerns and MUST NOT throw —
     * a failing observer must not break other observers or the transaction.
     */
    void onStatusChange(ParcelStatusEvent event);
}
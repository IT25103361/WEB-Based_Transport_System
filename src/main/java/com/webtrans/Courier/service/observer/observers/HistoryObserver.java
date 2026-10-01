package com.webtrans.Courier.service.observer.observers;

import com.webtrans.Courier.model.StatusHistory;
import com.webtrans.Courier.repository.StatusHistoryRepository;
import com.webtrans.Courier.service.observer.ParcelStatusEvent;
import com.webtrans.Courier.service.observer.ParcelStatusObserver;
import org.springframework.stereotype.Component;

/**
 * Observer #1 — persists every status change as a StatusHistory row.
 * This is what powers the timeline shown to senders and trackers.
 */
@Component
public class HistoryObserver implements ParcelStatusObserver {

    private final StatusHistoryRepository statusHistoryRepository;

    public HistoryObserver(StatusHistoryRepository statusHistoryRepository) {
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Override
    public void onStatusChange(ParcelStatusEvent event) {
        statusHistoryRepository.save(StatusHistory.builder()
                .parcel(event.getParcel())
                .status(event.getNewStatus())
                .note(event.getNote())
                .changedBy(event.getChangedBy())
                .lat(event.getLat())
                .lng(event.getLng())
                .build());
    }
}
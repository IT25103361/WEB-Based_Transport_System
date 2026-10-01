package com.webtrans.Courier.service.observer.observers;

import com.webtrans.Courier.dto.parcel.LocationBroadcast;
import com.webtrans.Courier.service.observer.ParcelStatusEvent;
import com.webtrans.Courier.service.observer.ParcelStatusObserver;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Observer #2 — broadcasts every status change to /topic/parcel/{id}/status
 * so any subscribed tracker page updates in real time.
 *
 * Note: live GPS movement is still pushed separately by MatchingService.pushLiveLocation()
 * (it uses /topic/parcel/{id}/location, and it fires on every location ping, not just
 * on status changes).
 */
@Component
public class WebSocketObserver implements ParcelStatusObserver {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketObserver(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onStatusChange(ParcelStatusEvent event) {

        // Reuse LocationBroadcast DTO for consistency with the location push
        LocationBroadcast payload = LocationBroadcast.builder()
                .parcelId(event.getParcel().getId())
                .trackingCode(event.getParcel().getTrackingCode())
                .partnerId(event.getChangedBy() != null ? event.getChangedBy().getId() : null)
                .partnerName(event.getChangedBy() != null ? event.getChangedBy().getName() : "System")
                .lat(event.getLat())
                .lng(event.getLng())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/parcel/" + event.getParcel().getId() + "/status",
                payload
        );
    }
}
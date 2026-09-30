package com.webtrans.school.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record StopUpdateRequest(
        String pickupStop,
        @JsonAlias({"dropoffStop", "drop_off_stop"})
        String dropOffStop,
        Long routeId,
        String routeName,
        String originCity,
        String destinationSchool,
        Long busId,
        Long morningBusId,
        String morningBusNumber,
        String morningBusName,
        String morningDriver,
        String morningPhone,
        Long returnBusId,
        String returnBusNumber,
        String returnBusName,
        String returnDriver,
        String returnPhone,
        String pickupTime,
        String expectedArrival
) {
    public StopUpdateRequest(String pickupStop, String dropOffStop) {
        this(pickupStop, dropOffStop, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
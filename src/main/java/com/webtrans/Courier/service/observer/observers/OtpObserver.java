package com.webtrans.Courier.service.observer.observers;

import com.webtrans.Courier.model.Parcel;
import com.webtrans.Courier.service.OtpService;
import com.webtrans.Courier.service.observer.ParcelStatusEvent;
import com.webtrans.Courier.service.observer.ParcelStatusObserver;
import org.springframework.stereotype.Component;

/**
 * Observer #3 — when a parcel arrives, generate a delivery OTP for the receiver.
 *
 * This isolates the "OTP generation" concern from the state machine.
 * In the future, if we want OTPs on other statuses too, we just change this one class.
 */
@Component
public class OtpObserver implements ParcelStatusObserver {

    private final OtpService otpService;

    public OtpObserver(OtpService otpService) {
        this.otpService = otpService;
    }

    @Override
    public void onStatusChange(ParcelStatusEvent event) {
        if (event.getNewStatus() == Parcel.Status.ARRIVED) {
            otpService.generateForParcel(event.getParcel());
        }
    }
}
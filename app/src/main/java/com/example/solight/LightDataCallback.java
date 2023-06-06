package com.example.solight;

import com.google.firebase.Timestamp;

public interface LightDataCallback {
    void onLightDataReceived(int batteryLevel, int current, int resistance, int temperature, int voltage, Timestamp turnOnTime, Timestamp turnOffTime);
    void onError(Exception e);
}
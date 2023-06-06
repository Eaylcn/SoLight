package com.example.solight;

import android.view.MenuItem;

public interface LightStatusCallback {
    void onLightStatusReceived(boolean isLightOn);
    void onLightStatusReceived(boolean isLightOn, MenuItem item);
    void onError(Exception e);
}

package com.github.thomasfox.saildata.sender;

import android.app.Activity;
import android.util.Log;

class BleConnectionWatchdog extends Thread {

    private static final String LOG_TAG ="saildata:BLEConnThread";

    private static final long CONNECTION_CHECK_SLEEP_TIME_MILLIS = 1000L;

    private static final long RECONNECT_SLEEP_TIME_MILLIS = 2000L;

    private static final long RECONNECT_INCOMPATIBLE_DEVICE_SLEEP_TIME_MILLIS = 10000L;

    private final BleSender bleSender;

    private final Activity activity;

    private final String address;

    private boolean shouldStop = false;

    public void close() {
        shouldStop = true;
    }

    public BleConnectionWatchdog(BleSender bleSender, Activity activity, String address) {
        this.bleSender = bleSender;
        this.activity = activity;
        this.address = address;
    }

    public void run() {
        while (!shouldStop) {
            try {
                if (bleSender.isConnected()) {
                    Log.d(LOG_TAG, "bleSender is connected, nothing to do");
                    sleepInternal(CONNECTION_CHECK_SLEEP_TIME_MILLIS);
                    continue;
                }
                bleSender.connectInternal(activity, address);
            } catch (RuntimeException connectException) {
                Log.w(LOG_TAG, "Error while connecting, disconnecting");
            }
            if (bleSender.isConnectionAttemptedToIncompatibleDevice()) {
                sleepInternal(RECONNECT_INCOMPATIBLE_DEVICE_SLEEP_TIME_MILLIS);
            }
            else {
                sleepInternal(RECONNECT_SLEEP_TIME_MILLIS);
            }
        }

        if (bleSender.isConnected()) {
            bleSender.closeCurrentBleConnection();
        }
    }

    private void sleepInternal(Long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (Exception ignored) {
        }
    }
}

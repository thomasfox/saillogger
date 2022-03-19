package com.github.thomasfox.saildata.location;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

public class FakeLocationProvider {

    private LocationProviderThread locationProviderThread;

    public FakeLocationProvider(LocationListener locationListener) {
        locationProviderThread = new LocationProviderThread(locationListener);
        locationProviderThread.start();
    }

    public void close() {
        locationProviderThread.close();
        locationProviderThread.interrupt();
        locationProviderThread = null;
    }


    private static class LocationProviderThread extends Thread {

        private LocationListener locationListener;

        private float currentBearing = 50f;

        private float currentSpeed = 0f;

        private float currentLatitude = 45f;

        private float currentLongitude = 9f;

        private boolean shouldStop = false;

        public LocationProviderThread(LocationListener locationListener) {
            this.locationListener = locationListener;
        }

        @Override
        public void run() {
            while (!shouldStop) {
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setBearing(currentBearing);
                location.setSpeed(currentSpeed);
                location.setLatitude(currentLatitude);
                location.setLongitude(currentLongitude);
                currentBearing += 3;
                if (currentBearing >= 360) {
                    currentBearing -= 360;
                }
                currentSpeed += 0.1;
                if (currentSpeed > 30) {
                    currentSpeed = 0;
                }
                currentLatitude += 0.0001f;
                currentLongitude = 9f + 0.0003f * currentSpeed;
                locationListener.onLocationChanged(location);
                try {
                    sleep(250L);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        public void close() {
            shouldStop = true;
        }
    }
}

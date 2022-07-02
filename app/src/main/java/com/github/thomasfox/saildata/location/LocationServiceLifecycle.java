package com.github.thomasfox.saildata.location;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;

/**
 * Manages the lifecycle of the Location service.
 *
 * Can start the Location Service and bind it to send updates to the Location Listener,
 * and unbind and stop the service.
 */
public class LocationServiceLifecycle {

    private LocationService locationService;

    private boolean locationServiceBound = false;

    private final Context context;

    private final LocationListenerHub locationListener;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
            locationService = binder.getService();
            locationServiceBound = true;
            locationService.registerCallback(locationListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationServiceBound = false;
        }
    };

    public LocationServiceLifecycle(
            @NonNull Context context,
            @NonNull LocationListenerHub locationListener) {
        this.context = context;
        this.locationListener = locationListener;
    }

    /**
     * Starts and binds to the location service to send location updates to the location listener.
     */
    public void start()  {
        Intent locationServiceIntent = new Intent(context, LocationService.class);
        context.startService(locationServiceIntent);

        context.bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbinds and stops the location service.
     */
    public void stop() {
        if (locationServiceBound) {
            locationService.clearCallback();
            context.unbindService(serviceConnection);
            locationServiceBound = false;
        }
        Intent locationServiceIntent = new Intent(context, LocationService.class);
        context.stopService(locationServiceIntent);
    }
}

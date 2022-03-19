package com.github.thomasfox.saildata.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.thomasfox.saildata.analyzer.TackDirectionChangeAnalyzer;
import com.github.thomasfox.saildata.logger.DataLogger;

/**
 * Requests location information from the android system and passes the information
 * to the places where location information is needed in the application.
 * Handles the android permissions necessary to access the location in the android system.
 */
public class LocationListenerHub implements LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2135;

    private final LocationManager locationManager;

    private final AppCompatActivity activity;

    private final DataLogger dataLogger;

    private final BluetoothLocationDisplayer bluetoothLocationDisplayer;

    private final ScreenLocationDisplayer screenLocationDisplayer;

    private final TackDirectionChangeAnalyzer tackDirectionChangeAnalyzer;

    private FakeLocationProvider fakeLocationProvider;

    private static final int LOCATION_POLLING_INTERVAL_MILLIS = 250;

    private static final int LOCATION_MIN_DISTANCE_METERS = 1;

    private static final boolean USE_FAKE_LOCATION = false;

    public LocationListenerHub(
            @NonNull AppCompatActivity activity,
            @NonNull DataLogger dataLogger,
            @NonNull ScreenLocationDisplayer screenLocationDisplayer,
            @NonNull BluetoothLocationDisplayer bluetoothLocationDisplayer,
            @NonNull TackDirectionChangeAnalyzer tackDirectionChangeAnalyzer) {
        this.activity = activity;
        this.screenLocationDisplayer = screenLocationDisplayer;
        this.dataLogger = dataLogger;
        this.bluetoothLocationDisplayer = bluetoothLocationDisplayer;
        this.tackDirectionChangeAnalyzer = tackDirectionChangeAnalyzer;

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
        else
        {
            registerLocationListener();
        }
    }

    public void onLocationChanged(Location location) {
        tackDirectionChangeAnalyzer.onLocationChanged(location);
        dataLogger.setLocation(location);
        screenLocationDisplayer.onLocationChanged(location);
        bluetoothLocationDisplayer.onLocationChanged(location);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}

    public void close()
    {
        stopLocationListener();
        if (fakeLocationProvider != null) {
            fakeLocationProvider.close();
            fakeLocationProvider = null;
        }
    }

    private void registerLocationListener()
    {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            if (USE_FAKE_LOCATION) {
                fakeLocationProvider = new FakeLocationProvider(this);
            }
            else {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        LOCATION_POLLING_INTERVAL_MILLIS,
                        LOCATION_MIN_DISTANCE_METERS,
                        this);
            }
            screenLocationDisplayer.displayWaitForFix();
        }
        else
        {
            screenLocationDisplayer.displayPermissionDenied();
        }
    }

    private void stopLocationListener()
    {
        locationManager.removeUpdates(this);
    }
}

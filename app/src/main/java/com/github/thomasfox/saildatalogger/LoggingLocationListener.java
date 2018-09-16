package com.github.thomasfox.saildatalogger;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.content.Context;

import com.github.thomasfox.saildatalogger.logger.DataLogger;

import java.util.Locale;

class LoggingLocationListener implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    /** radius of the earth in meters. */
    private static final double EARTH_RADIUS = 6371000;

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2135;

    private final TextView statusText;

    private final LocationManager locationManager;

    private final AppCompatActivity activity;

    private final DataLogger dataLogger;

    private Location startLocation;

    private static final int LOCATION_POLLING_INTERVAL_MILLIS = 2000;

    private static final int LOCATION_MIN_DISTANCE_METERS = 2;

    LoggingLocationListener(
            @NonNull AppCompatActivity activity,
            @NonNull TextView statusText,
            @NonNull DataLogger dataLogger) {
        this.activity = activity;
        this.statusText = statusText;
        this.dataLogger = dataLogger;
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
        if (startLocation == null) {
            startLocation = location;
        }
        statusText.setText(getLocationText(location));
        dataLogger.setLocation(location);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}

    public void close()
    {
        stopLocationListener();
    }

    private void registerLocationListener()
    {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_POLLING_INTERVAL_MILLIS,
                    LOCATION_MIN_DISTANCE_METERS,
                   this);
            statusText.setText(activity.getResources().getString(R.string.info_gps_acticated));
        }
        else
        {
            statusText.setText(activity.getResources().getString(R.string.err_gps_permission_denied));
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerLocationListener();
                } else {
                    statusText.setText(activity.getResources().getString(R.string.err_gps_permission_denied));
                }
            }
        }
    }

    private void stopLocationListener()
    {
        locationManager.removeUpdates(this);
        statusText.setText(activity.getResources().getString(R.string.info_gps_stopped));
        startLocation = null;
    }

    private String getLocationText(Location location) {
        String result = String.format(Locale.GERMAN, "(%.0f,%.0f)m",
                getX(location) - getX(startLocation),
                getY(location)  - getY(startLocation));
        if (location.hasAccuracy()) {
            result += " +/- " + location.getAccuracy() + "m";
        }
        if (location.hasBearing()) {
            result += ", bearing " + location.getBearing();
        }
        return result;
    }

    private double getY(Location location)
    {
        return location.getLatitude() / 180 * Math.PI * EARTH_RADIUS;
    }

    private double getX(Location location)
    {
        return location.getLongitude() / 180 * Math.PI
                * Math.cos(location.getLatitude() / 180 * Math.PI)
                * EARTH_RADIUS;
    }
}

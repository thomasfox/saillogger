package com.github.thomasfox.saildata;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.thomasfox.saildata.logger.DataLogger;
import com.github.thomasfox.saildata.sender.BleSender;

import java.util.Locale;

class LoggingLocationListener implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final double EARTH_RADIUS_IN_METERS = 6371000;

    private static final float METERS_PER_SECOND_IN_KNOTS = 1.94384f;

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2135;

    private final TextView statusTextView;

    private final TextView locationTextView;

    private final TextView speedTextView;

    private final TextView bearingTextView;

    private final LocationManager locationManager;

    private final AppCompatActivity activity;

    private final DataLogger dataLogger;

    private final BleSender bluetoothSender;

    private Location startLocation;

    private static final int LOCATION_POLLING_INTERVAL_MILLIS = 250;

    private static final int LOCATION_MIN_DISTANCE_METERS = 1;

    LoggingLocationListener(
            @NonNull AppCompatActivity activity,
            @NonNull TextView statusTextView,
            @NonNull TextView locationTextView,
            @NonNull TextView speedTextView,
            @NonNull TextView bearingTextView,
            @NonNull DataLogger dataLogger,
            BleSender bluetoothSender) {
        this.activity = activity;
        this.statusTextView = statusTextView;
        this.locationTextView = locationTextView;
        this.speedTextView = speedTextView;
        this.bearingTextView = bearingTextView;
        this.dataLogger = dataLogger;
        this.bluetoothSender = bluetoothSender;
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
        if (location.hasAccuracy()) {
            statusTextView.setText(activity.getResources().getString(
                    R.string.info_gps_accuracy,
                    location.getAccuracy() + "m"));
        }
        else {
            statusTextView.setText(activity.getResources().getString(
                    R.string.info_gps_fix));
        }
        locationTextView.setText(getLocationText(location));
        speedTextView.setText(getSpeedText(location));
        bearingTextView.setText(getBearingText(location));
        dataLogger.setLocation(location);
        if (bluetoothSender != null) {
            bluetoothSender.sendLineIfConnected(getSpeedText(location));
        }
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
            statusTextView.setText(activity.getResources().getString(R.string.info_gps_wait_for_fix));
        }
        else
        {
            statusTextView.setText(activity.getResources().getString(R.string.err_gps_permission_denied));
        }
    }

    @Override
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
                    statusTextView.setText(activity.getResources().getString(R.string.err_gps_permission_denied));
                }
            }
        }
    }

    private void stopLocationListener()
    {
        locationManager.removeUpdates(this);
        statusTextView.setText(activity.getResources().getString(R.string.info_gps_stopped));
        locationTextView.setText(activity.getResources().getString(R.string.status_standby));
        speedTextView.setText(activity.getResources().getString(R.string.speed_no_value_text));
        bearingTextView.setText(activity.getResources().getString(R.string.bearing_no_value_text));
        startLocation = null;
    }

    private String getLocationText(Location location) {
        String result = String.format(Locale.GERMAN, "(%.0f,%.0f)m",
                getX(location) - getX(startLocation),
                getY(location)  - getY(startLocation));
        return result;
    }

    private double getY(Location location)
    {
        return location.getLatitude() / 180 * Math.PI * EARTH_RADIUS_IN_METERS;
    }

    private double getX(Location location)
    {
        return location.getLongitude() / 180 * Math.PI
                * Math.cos(location.getLatitude() / 180 * Math.PI)
                * EARTH_RADIUS_IN_METERS;
    }

    private String getSpeedText(Location location) {
        String result = String.format(Locale.ENGLISH, "%.1f",
                location.getSpeed() * METERS_PER_SECOND_IN_KNOTS);

        return result;
    }

    private String getBearingText(Location location) {
        String result = String.format(Locale.GERMAN, "%.0f°",
                location.getBearing());
        return result;
    }
}

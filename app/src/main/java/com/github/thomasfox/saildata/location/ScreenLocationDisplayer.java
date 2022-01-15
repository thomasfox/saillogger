package com.github.thomasfox.saildata.location;

import static com.github.thomasfox.saildata.location.LocationConstants.EARTH_RADIUS_IN_METERS;
import static com.github.thomasfox.saildata.location.LocationConstants.METERS_PER_SECOND_IN_KNOTS;

import android.location.Location;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.thomasfox.saildata.R;

import java.util.Locale;

/**
 * Displays location information on the device's screen by filling the location, speed
 * and bearing fields on the android device's screen.
 * Also fills the gps status field on the screen with the current status.
 */
public class ScreenLocationDisplayer {

    private final AppCompatActivity activity;

    private final TextView gpsStatusTextView;

    private final TextView locationTextView;

    private final TextView speedTextView;

    private final TextView bearingTextView;

    private Location startLocation;

    public ScreenLocationDisplayer(
            @NonNull AppCompatActivity activity,
            @NonNull TextView gpsStatusTextView,
            @NonNull TextView locationTextView,
            @NonNull TextView speedTextView,
            @NonNull TextView bearingTextView) {
        this.activity = activity;
        this.gpsStatusTextView = gpsStatusTextView;
        this.locationTextView = locationTextView;
        this.speedTextView = speedTextView;
        this.bearingTextView = bearingTextView;
    }

    public void onLocationChanged(Location location) {
        if (startLocation == null) {
            startLocation = location;
        }
        if (location.hasAccuracy()) {
            gpsStatusTextView.setText(
                    activity.getResources().getString(
                            R.string.status_gps_tag,
                            activity.getResources().getString(
                                    R.string.info_gps_accuracy,
                                    location.getAccuracy() + "m")));
        }
        else {
            statusChanged(R.string.status_fix);
        }
        locationTextView.setText(getLocationText(location));
        speedTextView.setText(getSpeedText(location));
        bearingTextView.setText(getBearingText(location));
    }

    public void displayWaitForFix() {
        statusChanged(R.string.status_wait_for_fix);
    }

    public void displayPermissionDenied() {
        statusChanged(R.string.status_permission_denied);
    }

    public void close() {
        statusChanged(R.string.status_stopped);
        locationTextView.setText(activity.getResources().getString(R.string.status_standby));
        speedTextView.setText(activity.getResources().getString(R.string.speed_no_value_text));
        bearingTextView.setText(activity.getResources().getString(R.string.bearing_no_value_text));
        startLocation = null;
    }

    private String getLocationText(Location location) {
        return String.format(Locale.GERMAN, "(%.0f,%.0f)m",
                getX(location) - getX(startLocation),
                getY(location)  - getY(startLocation));
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
        return String.format(Locale.ENGLISH, "%.1f",
                location.getSpeed() * METERS_PER_SECOND_IN_KNOTS);
    }

    private String getBearingText(Location location) {
        return String.format(Locale.GERMAN, "%.0f°",
                location.getBearing());
    }

    private void statusChanged(int statusTextResourceId) {
        gpsStatusTextView.setText(
                activity.getResources().getString(R.string.status_gps_tag,
                        activity.getResources().getString(statusTextResourceId)));

    }
}

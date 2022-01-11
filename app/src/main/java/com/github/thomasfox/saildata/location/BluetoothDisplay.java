package com.github.thomasfox.saildata.location;

import static com.github.thomasfox.saildata.location.LocationConstants.METERS_PER_SECOND_IN_KNOTS;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.thomasfox.saildata.R;
import com.github.thomasfox.saildata.sender.BleSender;

import java.util.Locale;

public class BluetoothDisplay {

    private final AppCompatActivity activity;

    private final BleSender bluetoothSender;

    public BluetoothDisplay(
            @NonNull AppCompatActivity activity,
            @NonNull BleSender bluetoothSender) {
        this.activity = activity;
        this.bluetoothSender = bluetoothSender;
    }

    public void onLocationChanged(Location location) {
        bluetoothSender.sendSpeedIfConnected(getSpeedText(location));
        bluetoothSender.sendBearingStringIfConnected(getBearingText(location));
    }

    public void close() {
        bluetoothSender.sendSpeedIfConnected(activity.getResources().getString(R.string.speed_no_value_text));
        bluetoothSender.sendBearingStringIfConnected(activity.getResources().getString(R.string.bearing_no_value_text));
        bluetoothSender.close();
    }

    private String getSpeedText(Location location) {
        return String.format(Locale.ENGLISH, "%.1f",
                location.getSpeed() * METERS_PER_SECOND_IN_KNOTS);
    }

    private String getBearingText(Location location) {
        return String.format(Locale.GERMAN, "%.0f°",
                location.getBearing());
    }
}

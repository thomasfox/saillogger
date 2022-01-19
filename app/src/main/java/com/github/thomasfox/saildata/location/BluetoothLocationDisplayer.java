package com.github.thomasfox.saildata.location;

import static com.github.thomasfox.saildata.location.LocationConstants.METERS_PER_SECOND_IN_KNOTS;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.thomasfox.saildata.R;
import com.github.thomasfox.saildata.analyzer.TackDirectionChangeAnalyzer;
import com.github.thomasfox.saildata.sender.BleSender;

import java.util.Locale;

/**
 * Displays location information on a connected bluetooth display by sending the current speed,
 * and direction to the connected BLE device.
 */
public class BluetoothLocationDisplayer {

    private final AppCompatActivity activity;

    private final BleSender bluetoothSender;

    private final TackDirectionChangeAnalyzer tackDirectionChangeAnalyzer;

    public BluetoothLocationDisplayer(
            @NonNull AppCompatActivity activity,
            @NonNull BleSender bluetoothSender,
            @NonNull TackDirectionChangeAnalyzer tackDirectionChangeAnalyzer) {
        this.activity = activity;
        this.bluetoothSender = bluetoothSender;
        this.tackDirectionChangeAnalyzer = tackDirectionChangeAnalyzer;
    }

    public void onLocationChanged(Location location) {
        bluetoothSender.sendSpeedIfConnected(getSpeedText(location));
        bluetoothSender.sendBearingStringIfConnected(getBearingText(location));
        bluetoothSender.sendBearingBarIfConnected(getBearingBarText());
    }

    public void close() {
        bluetoothSender.sendSpeedIfConnected(activity.getResources().getString(R.string.speed_no_value_text));
        bluetoothSender.sendBearingStringIfConnected(activity.getResources().getString(R.string.bearing_no_value_text));
        bluetoothSender.sendBearingBarIfConnected("0");
    }

    private String getSpeedText(Location location) {
        return String.format(Locale.ENGLISH, "%.1f",
                location.getSpeed() * METERS_PER_SECOND_IN_KNOTS);
    }

    private String getBearingText(Location location) {
        return String.format(Locale.GERMAN, "%.0f°",
                location.getBearing());
    }

    private String getBearingBarText() {
        // Bearing bar values are from -100 to 100.
        // We map that to -25 degrees to +25 degrees off the current tack.
        Float directionRelativeToTackDirection
                = tackDirectionChangeAnalyzer.getDirectionRelativeToTackDirection();
        if (directionRelativeToTackDirection == null) {
            return null;
        }
        return String.format(Locale.GERMAN, "%.0f°",
                directionRelativeToTackDirection * 4);
    }
}

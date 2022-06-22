package com.github.thomasfox.saildata;

import android.Manifest;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.github.thomasfox.saildata.camera.CameraManager;
import com.github.thomasfox.saildata.logger.Files;

/**
 * Handles "Start logging" ond "Stop logging" events from the user.
 */
public class StartStopLoggingClickListener implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 2135;

    private final TextView locationTextView;

    private final TextView bleStatusTextView;

    private CameraManager cameraManager;

    private final MainActivity activity;

    public StartStopLoggingClickListener(
            TextView locationTextView,
            TextView bleStatusTextView,
            MainActivity activity) {
        this.locationTextView = locationTextView;
        this.bleStatusTextView = bleStatusTextView;
        this.activity = activity;
    }

    @Override
    public void onClick(View view) {
        boolean startLogging = ((ToggleButton) view).isChecked();
        if (startLogging) {
            startLogging();
        }
        else {
            DialogFragment stopLoggingDialogFragment = new StopLoggingDialogFragment();
            stopLoggingDialogFragment.show(activity.getFragmentManager(), "stopLoggingDialog");
        }
    }

    private void startLogging() {
        requestLocationPermissionIfNeeded();
        int trackFileNumber = Files.getTrackFileNumber(activity);
        activity.getLocationListener().startLogging(
                locationTextView,
                trackFileNumber,
                bleStatusTextView);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        if (defaultSharedPreferences.getBoolean(SettingsActivity.SETTINGS_KEY_RECORD_VIDEO, false)) {
            cameraManager = new CameraManager(activity, trackFileNumber);
        }
        activity.getScreenManager().disableScreenOff();
        if (defaultSharedPreferences.getBoolean(
                SettingsActivity.SETTINGS_KEY_DIM_SCREEN_WHILE_LOGGING,
                false)) {
            activity.getScreenManager().minimizeBrightness();
         }
    }

    public void stopLogging() {
        activity.getLocationListener().stopLogging();
        activity.getScreenLocationDisplayer().stopLogging();
        if (cameraManager != null) {
            cameraManager.close();
            cameraManager = null;
        }
        activity.getScreenManager().restoreSystemBrightness();
        activity.getScreenManager().allowScreenOff();
    }

    private void requestLocationPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }
}

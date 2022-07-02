package com.github.thomasfox.saildata;

import android.Manifest;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.github.thomasfox.saildata.camera.CameraManager;
import com.github.thomasfox.saildata.location.LocationListenerHub;
import com.github.thomasfox.saildata.location.LocationServiceLifecycle;
import com.github.thomasfox.saildata.location.ScreenLocationDisplayer;
import com.github.thomasfox.saildata.logger.Files;
import com.github.thomasfox.saildata.screen.ScreenManager;

/**
 * Handles "Start logging" ond "Stop logging" events from the user.
 */
public class StartStopLoggingClickListener implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 2135;

    private final TextView locationTextView;

    private final TextView bleStatusTextView;

    private final ScreenLocationDisplayer screenLocationDisplayer;

    private CameraManager cameraManager;

    private final LocationListenerHub locationListener;

    private final MainActivity activity;

    private LocationServiceLifecycle locationServiceLifecycle;

    private final ScreenManager screenManager;

    public StartStopLoggingClickListener(
            @NonNull TextView locationTextView,
            @NonNull TextView bleStatusTextView,
            @NonNull ScreenLocationDisplayer screenLocationDisplayer,
            @NonNull ScreenManager screenManager,
            @NonNull MainActivity activity) {
        this.locationTextView = locationTextView;
        this.bleStatusTextView = bleStatusTextView;
        this.screenLocationDisplayer = screenLocationDisplayer;
        this.screenManager = screenManager;
        this.activity = activity;

        this.locationListener = new LocationListenerHub(activity, screenLocationDisplayer);
        locationServiceLifecycle = new LocationServiceLifecycle(activity, locationListener);
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
        locationServiceLifecycle = new LocationServiceLifecycle(activity, locationListener);
        locationServiceLifecycle.start();

        requestLocationPermissionIfNeeded();
        int trackFileNumber = Files.getTrackFileNumber(activity);
        locationListener.startLogging(
                locationTextView,
                trackFileNumber,
                bleStatusTextView);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        if (defaultSharedPreferences.getBoolean(SettingsActivity.SETTINGS_KEY_RECORD_VIDEO, false)) {
            cameraManager = new CameraManager(activity, trackFileNumber);
        }
        screenManager.disableScreenOff();
        if (defaultSharedPreferences.getBoolean(
                SettingsActivity.SETTINGS_KEY_DIM_SCREEN_WHILE_LOGGING,
                false)) {
            screenManager.minimizeBrightness();
         }
    }

    public void stopLogging() {
        if (locationServiceLifecycle != null) {
            locationServiceLifecycle.stop();
            locationServiceLifecycle = null;
        }

        locationListener.stopLogging();
        screenLocationDisplayer.stopLogging();
        if (cameraManager != null) {
            cameraManager.close();
            cameraManager = null;
        }
        screenManager.restoreSystemBrightness();
        screenManager.allowScreenOff();
    }

    private void requestLocationPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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

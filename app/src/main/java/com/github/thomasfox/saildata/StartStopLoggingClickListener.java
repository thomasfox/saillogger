package com.github.thomasfox.saildata;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.github.thomasfox.saildata.camera.CameraManager;
import com.github.thomasfox.saildata.location.BluetoothDisplay;
import com.github.thomasfox.saildata.location.LocationListenerHub;
import com.github.thomasfox.saildata.location.LocationScreenDisplay;
import com.github.thomasfox.saildata.logger.DataLogger;
import com.github.thomasfox.saildata.logger.Files;
import com.github.thomasfox.saildata.screen.ScreenManager;
import com.github.thomasfox.saildata.sender.BleSender;

public class StartStopLoggingClickListener implements View.OnClickListener {

    private final TextView locationTextView;

    private final TextView gpsStatusTextView;

    private final TextView bleStatusTextView;

    private final TextView speedTextView;

    private final TextView bearingTextView;

    private LocationListenerHub locationListener;

    private LoggingSensorListener compassListener;

    private DataLogger dataLogger;

    private LocationScreenDisplay locationScreenDisplay;

    private BluetoothDisplay bluetoothDisplay;

    private CameraManager cameraManager;

    private final AppCompatActivity activity;

    private final ScreenManager screenManager;

    public StartStopLoggingClickListener(
            TextView locationTextView,
            TextView gpsStatusTextView,
            TextView bleStatusTextView,
            TextView speedTextView,
            TextView bearingTextView,
            MainActivity activity) {
        this.locationTextView = locationTextView;
        this.gpsStatusTextView = gpsStatusTextView;
        this.bleStatusTextView = bleStatusTextView;
        this.speedTextView = speedTextView;
        this.bearingTextView = bearingTextView;
        this.activity = activity;
        this.screenManager = activity.getScreenManager();
    }

    @Override
    public void onClick(View view) {
        boolean startLogging = ((ToggleButton) view).isChecked();
        if (startLogging) {
            startLogging();
        } else if (dataLogger != null) {
            DialogFragment stopLoggingDialogFragment = new StopLoggingDialogFragment();
            stopLoggingDialogFragment.show(activity.getFragmentManager(), "stopLoggingDialog");
        }
    }

    private void startLogging() {
        int trackFileNumber = Files.getTrackFileNumber(activity);
        dataLogger = new DataLogger(activity, locationTextView, trackFileNumber);
        BleSender bluetoothSender = new BleSender(activity, bleStatusTextView);
        bluetoothDisplay = new BluetoothDisplay(activity, bluetoothSender);
        locationScreenDisplay = new LocationScreenDisplay(
                activity,
                gpsStatusTextView,
                locationTextView,
                speedTextView,
                bearingTextView);
        locationListener = new LocationListenerHub(
                activity,
                dataLogger,
                locationScreenDisplay,
                bluetoothDisplay);
        compassListener = new LoggingSensorListener(activity, dataLogger);
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
        locationListener.close();
        locationListener = null;
        compassListener.close();
        compassListener = null;
        dataLogger.close();
        dataLogger = null;
        locationScreenDisplay.close();
        locationScreenDisplay = null;
        bluetoothDisplay.close();
        bluetoothDisplay = null;
        if (cameraManager != null) {
            cameraManager.close();
            cameraManager = null;
        }
        screenManager.restoreSystemBrightness();
        screenManager.allowScreenOff();
    }
}

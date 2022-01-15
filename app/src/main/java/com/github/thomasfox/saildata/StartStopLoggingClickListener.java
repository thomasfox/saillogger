package com.github.thomasfox.saildata;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.github.thomasfox.saildata.analyzer.TackDirectionChangeAnalyzer;
import com.github.thomasfox.saildata.camera.CameraManager;
import com.github.thomasfox.saildata.location.BluetoothLocationDisplayer;
import com.github.thomasfox.saildata.location.LocationListenerHub;
import com.github.thomasfox.saildata.location.ScreenLocationDisplayer;
import com.github.thomasfox.saildata.logger.DataLogger;
import com.github.thomasfox.saildata.logger.Files;
import com.github.thomasfox.saildata.screen.ScreenManager;
import com.github.thomasfox.saildata.sender.BleSender;

/**
 * Handles "Start logging" ond "Stop logging" events from the user.
 * Handles the lifecycle of the components which are needed while logging is active.
 */
public class StartStopLoggingClickListener implements View.OnClickListener {

    private final TextView locationTextView;

    private final TextView gpsStatusTextView;

    private final TextView bleStatusTextView;

    private final TextView speedTextView;

    private final TextView bearingTextView;

    private LocationListenerHub locationListener;

    private LoggingSensorListener compassListener;

    private DataLogger dataLogger;

    private ScreenLocationDisplayer locationScreenDisplay;

    private BluetoothLocationDisplayer bluetoothLocationDisplayer;

    private BleSender bleSender;

    private TackDirectionChangeAnalyzer tackDirectionChangeAnalyzer;

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
        tackDirectionChangeAnalyzer = new TackDirectionChangeAnalyzer();
        bleSender = new BleSender(activity, bleStatusTextView);
        bluetoothLocationDisplayer = new BluetoothLocationDisplayer(
                activity,
                bleSender,
                tackDirectionChangeAnalyzer);
        locationScreenDisplay = new ScreenLocationDisplayer(
                activity,
                gpsStatusTextView,
                locationTextView,
                speedTextView,
                bearingTextView);
        locationListener = new LocationListenerHub(
                activity,
                dataLogger,
                locationScreenDisplay,
                bluetoothLocationDisplayer,
                tackDirectionChangeAnalyzer);
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
        bluetoothLocationDisplayer.close();
        bluetoothLocationDisplayer = null;
        bleSender.close();
        bleSender = null;
        tackDirectionChangeAnalyzer = null;
        if (cameraManager != null) {
            cameraManager.close();
            cameraManager = null;
        }
        screenManager.restoreSystemBrightness();
        screenManager.allowScreenOff();
    }
}

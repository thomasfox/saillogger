package com.github.thomasfox.saildata;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.github.thomasfox.saildata.camera.CameraManager;
import com.github.thomasfox.saildata.logger.DataLogger;
import com.github.thomasfox.saildata.logger.Files;
import com.github.thomasfox.saildata.screen.ScreenManager;
import com.github.thomasfox.saildata.sender.BLESender;

class EnableLoggingClickListener implements View.OnClickListener {

    private final TextView statusTextView;

    private final TextView gpsStatusTextView;

    private final TextView bleStatusTextView;

    private TextView speedTextView;

    private TextView bearingTextView;

    private LoggingLocationListener locationListener;

    private LoggingSensorListener compassListener;

    private DataLogger dataLogger;

    private BLESender bluetoothSender;

    private CameraManager cameraManager;

    private final AppCompatActivity activity;

    private final ScreenManager screenManager;

    EnableLoggingClickListener(
            TextView statusTextView,
            TextView gpsStatusTextView,
            TextView bleStatusTextView,
            TextView speedTextView,
            TextView bearingTextView,
            MainActivity activity) {
        this.statusTextView = statusTextView;
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
        dataLogger = new DataLogger(activity, statusTextView, trackFileNumber);
        bluetoothSender = new BLESender(activity, bleStatusTextView);
        locationListener = new LoggingLocationListener(
                activity,
                statusTextView,
                speedTextView,
                bearingTextView,
                dataLogger,
                bluetoothSender);
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

    void stopLogging() {
        locationListener.close();
        locationListener = null;
        bluetoothSender.close();
        bluetoothSender = null;
        compassListener.close();
        compassListener = null;
        dataLogger.close();
        dataLogger = null;
        if (cameraManager != null) {
            cameraManager.close();
            cameraManager = null;
        }
        screenManager.restoreSystemBrightness();
        screenManager.allowScreenOff();

    }
}

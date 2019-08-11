package com.github.thomasfox.saildatalogger;

import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.thomasfox.saildatalogger.camera.CameraManager;
import com.github.thomasfox.saildatalogger.logger.DataLogger;
import com.github.thomasfox.saildatalogger.logger.Files;
import com.github.thomasfox.saildatalogger.screen.ScreenManager;
import com.github.thomasfox.saildatalogger.state.Settings;

class EnableLoggingClickListener implements View.OnClickListener {

    private final TextView statusText;

    private LoggingLocationListener locationListener;

    private LoggingSensorListener compassListener;

    private DataLogger dataLogger;

    private CameraManager cameraManager;

    private final AppCompatActivity activity;

    private final ScreenManager screenManager;

    EnableLoggingClickListener(
            TextView statusText,
            MainActivity activity) {
        this.statusText = statusText;
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
        int trackFileNumber = Files.getTrackFileNumber();
        dataLogger = new DataLogger(activity, statusText, trackFileNumber);
        locationListener = new LoggingLocationListener(activity, statusText, dataLogger);
        compassListener = new LoggingSensorListener(activity, dataLogger);
        if (Settings.recordVideo) {
            cameraManager = new CameraManager(activity, trackFileNumber);
        }
        screenManager.disableScreenOff();
        if (Settings.dimScreen) {
            screenManager.minimizeBrightness();
         }
    }

    void stopLogging() {
        locationListener.close();
        locationListener = null;
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

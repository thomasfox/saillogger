package com.github.thomasfox.saildatalogger;

import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.thomasfox.saildatalogger.camera.CameraManager;
import com.github.thomasfox.saildatalogger.logger.DataLogger;
import com.github.thomasfox.saildatalogger.logger.Files;
import com.github.thomasfox.saildatalogger.state.Settings;

class EnableLoggingClickListener implements View.OnClickListener {

    private final TextView statusText;

    private LoggingLocationListener locationListener;

    private LoggingSensorListener compassListener;

    private DataLogger dataLogger;

    private CameraManager cameraManager;

    private final AppCompatActivity activity;

    private Float oldBrightness;

    EnableLoggingClickListener(TextView statusText, AppCompatActivity activity) {
        this.statusText = statusText;
        this.activity = activity;
    }

    @Override
    public void onClick(View view) {
        boolean loggingEnabled = ((ToggleButton) view).isChecked();
        if (loggingEnabled) {
            int trackFileNumber = Files.getTrackFileNumber();
            dataLogger = new DataLogger(activity, statusText, trackFileNumber);
            locationListener = new LoggingLocationListener(activity, statusText, dataLogger);
            compassListener = new LoggingSensorListener(activity, dataLogger);
            if (Settings.recordVideo) {
                cameraManager = new CameraManager(activity, trackFileNumber);
            }

            keepScreenOn();
            adjustScreenBrightnessForLogging();
        } else if (dataLogger != null) {
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
            restoreBrightnessAndAllowScreenOff();
       }
    }

    private void keepScreenOn() {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void adjustScreenBrightnessForLogging() {
        if (Settings.dimScreen) {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            oldBrightness = lp.screenBrightness;
            lp.screenBrightness = 0.01f;
            activity.getWindow().setAttributes(lp);
        }
        else {
            oldBrightness = null;
        }
    }

    private void restoreBrightnessAndAllowScreenOff() {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        if (oldBrightness != null) {
            lp.screenBrightness = oldBrightness;
        }
        activity.getWindow().setAttributes(lp);
    }
}

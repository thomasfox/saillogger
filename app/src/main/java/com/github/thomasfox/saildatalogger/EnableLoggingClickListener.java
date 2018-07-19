package com.github.thomasfox.saildatalogger;

import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.thomasfox.saildatalogger.logger.DataLogger;
import com.github.thomasfox.saildatalogger.logger.Files;

public class EnableLoggingClickListener implements View.OnClickListener {

    private TextView statusText;

    private LoggingLocationListener locationListener;

    private LoggingSensorListener compassListener;

    private DataLogger dataLogger;

    private CameraManager cameraManager;

    private AppCompatActivity activity;

    EnableLoggingClickListener(TextView statusText, AppCompatActivity activity) {
        this.statusText = statusText;
        this.activity = activity;
    }

    @Override
    public void onClick(View view) {
        boolean loggingEnabled = ((ToggleButton) view).isChecked();
        if (loggingEnabled) {
            dataLogger = new DataLogger(activity, statusText, Files.getTrackFile());
            locationListener = new LoggingLocationListener(activity, statusText, dataLogger);
            compassListener = new LoggingSensorListener(activity, dataLogger);
            cameraManager = new CameraManager(activity);
        } else if (dataLogger != null) {
            locationListener.close();
            locationListener = null;
            compassListener.close();
            compassListener = null;
            dataLogger.close();
            dataLogger = null;
            cameraManager.close();
            cameraManager = null;
       }
    }
}

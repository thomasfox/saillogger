package com.github.thomasfox.saildatalogger;

import android.support.v7.app.AppCompatActivity;

import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;

public class EnableLoggingClickListener implements View.OnClickListener {

    private static String TRACK_FILE_NAME_PREFIX = "track";
    private static String TRACK_FILE_NAME_SUFFIX = ".track";

    private TextView statusText;

    private LoggingLocationListener locationRecorder;

    private AppCompatActivity activity;

    EnableLoggingClickListener(TextView statusText, AppCompatActivity activity) {
        this.statusText = statusText;
        this.activity = activity;
    }

    @Override
    public void onClick(View view) {
        boolean loggingEnabled = ((ToggleButton) view).isChecked();
        if (loggingEnabled) {
            locationRecorder = new LoggingLocationListener(activity, statusText, getTrackFile());
        } else if (locationRecorder != null) {
            locationRecorder.close();
            locationRecorder = null;
        }
    }

    private File getStorageDir() {
        // Get the directory for the app's private pictures directory.
        File file = new File(Environment.getExternalStorageDirectory() + "/saillogger");
        if (!file.mkdirs()) {
            statusText.setText(activity.getResources().getString(R.string.err_create_storage_dir));
        }
        return file;
    }

    private File getTrackFile() {
        return new File(getStorageDir(), TRACK_FILE_NAME_PREFIX + getTrackFileNumber() + TRACK_FILE_NAME_SUFFIX);
    }

    private Integer getTrackFileNumber() {
        File dir = getStorageDir();
        File[] files = dir.listFiles();
        int nextNumber = 1;
        for (File file : files) {
            if (file.getName().startsWith(TRACK_FILE_NAME_PREFIX) && file.getName().endsWith(TRACK_FILE_NAME_SUFFIX)) {
                String trackNumberString = file.getName().substring(
                        TRACK_FILE_NAME_PREFIX.length(), file.getName().length() - TRACK_FILE_NAME_SUFFIX.length());

                Integer trackNumber;
                try {
                    trackNumber = Integer.parseInt(trackNumberString);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (trackNumber >= nextNumber) {
                    nextNumber = trackNumber + 1;
                }
            }
        }
        return nextNumber;
    }

}

package com.github.thomasfox.saildata.logger;

import android.app.DialogFragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.thomasfox.saildata.R;
import com.github.thomasfox.saildata.ui.WroteFileDialogFragment;

import java.io.File;
import java.io.IOException;

public class DataLogger {
    private final AppCompatActivity activity;

    private final TextView statusText;

    private final File storageFile;

    private LoggingDataWriter dataWriter;

    public DataLogger(AppCompatActivity activity, TextView statusText, int trackFileNumber)
    {
        this.activity = activity;
        this.statusText = statusText;
        this.storageFile = Files.getTrackFile(trackFileNumber, activity);
        try {
            if (!isExternalStorageWritable())
            {
                throw new RuntimeException("External Storage not writeable");
            }
            if (!storageFile.exists()) {
                if (!storageFile.createNewFile()) {
                    throw new RuntimeException("File creation failed");
                }
            }
            dataWriter = new LoggingDataWriter(storageFile);
            dataWriter.startForAppVersion(activity.getResources().getString(R.string.app_version));
        } catch (Exception e) {
            statusText.setText(String.format(
                    activity.getResources().getString(R.string.err_write_track_data),
                    e.getMessage()));
        }
    }

    private void write(LoggingData toWrite) {
        try {
            if (dataWriter != null) {
                dataWriter.write(toWrite);
            }
        } catch (Exception e) {
            statusText.setText(String.format(
                    activity.getResources().getString(R.string.err_write_track_data),
                    e.getMessage()));
        }
    }

    public void close()
    {
        if (dataWriter != null) {
            try {
                dataWriter.close();
                dataWriter = null;
            }
            catch (IOException e)
            {
                statusText.setText(String.format(
                        activity.getResources().getString(R.string.err_close_track_data_file),
                        e.getMessage()));
            }
        }
        DialogFragment dialogFragment = new WroteFileDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(
                WroteFileDialogFragment.FILE_LOCATION_BUNDLE_KEY,
                storageFile.getAbsolutePath());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(activity.getFragmentManager(), "wroteFileDialog");
    }

    public synchronized void setLocation(Location location) {
        LoggingData locationData = new LoggingData();
        locationData.setLocation(location);
        write(locationData);
    }

    public synchronized void setMagneticField(float[] values) {
        LoggingData locationData = new LoggingData();
        locationData.setMagneticField(values);
        write(locationData);
    }

    public synchronized void setAcceleration(float[] values) {
        LoggingData locationData = new LoggingData();
        locationData.setAcceleration(values);
        write(locationData);
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}

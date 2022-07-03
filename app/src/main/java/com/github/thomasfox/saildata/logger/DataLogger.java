package com.github.thomasfox.saildata.logger;

import android.app.DialogFragment;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.JsonWriter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.thomasfox.saildata.R;
import com.github.thomasfox.saildata.ui.WroteFileDialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataLogger {
    /** The current sensor readings. */
    private final LoggingData currentData = new LoggingData();

    private final AppCompatActivity activity;

    private final TextView statusText;

    private final File storageFile;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd.MM.yyyy' 'HH:mm:ss.SSSZ",
            Locale.GERMANY);

    private JsonWriter jsonWriter;

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
            jsonWriter = new JsonWriter(new OutputStreamWriter(
                    new FileOutputStream(storageFile),
                    StandardCharsets.UTF_8));
            jsonWriter.setIndent("");
            Date startDate = new Date();
            jsonWriter.beginObject()
                    .name("start")
                    .beginObject()
                    .name("format")
                    .value("v1.5")
                    .name("loggedBy")
                    .value("saildata")
                    .name("loggedByVersion")
                    .value(activity.getResources().getString(R.string.app_version))
                    .name("startT")
                    .value(startDate.getTime())
                    .name("startTFormatted")
                    .value(dateFormat.format(startDate))
                    .name("recordedByManufacturer")
                    .value(Build.MANUFACTURER)
                    .name("recordedByModel")
                    .value(Build.MODEL)
                    .endObject()
                    .name("track");
            jsonWriter.beginArray();
        } catch (Exception e) {
            statusText.setText(String.format(
                    activity.getResources().getString(R.string.err_write_track_data),
                    e.getMessage()));
        }
    }

    public synchronized void setLocation(Location location) {
        currentData.setLocation(location);
        write();
        currentData.reset();
    }

    public synchronized void setMagneticField(float[] values) {
        currentData.setMagneticField(values);
        write();
        currentData.reset();
    }

    public synchronized void setAcceleration(float[] values) {
        currentData.setAcceleration(values);
        write();
        currentData.reset();
    }

    private void write() {
        if (jsonWriter != null)
        {
            try {
                jsonWriter.beginObject();
                if (currentData.hasLocation()) {
                    jsonWriter.name("locT").value(currentData.locationTime)
                            .name("locAcc").value(currentData.locationAccuracy)
                            .name("locLat").value(currentData.latitude)
                            .name("locLong").value(currentData.longitude)
                            .name("locBear").value(currentData.locationBearing)
                            .name("locVel").value(currentData.locationVelocity)
                            .name("locAlt").value(currentData.locationAltitude)
                            .name("locDevT").value(currentData.locationDeviceTime);
                }
                if (currentData.hasMagneticField()) {
                    jsonWriter.name("magT").value(currentData.magneticFieldTime)
                            .name("magX").value(currentData.magneticFieldX)
                            .name("magY").value(currentData.magneticFieldY)
                            .name("magZ").value(currentData.magneticFieldZ);
                }
                if (currentData.hasAcceleration()) {
                    jsonWriter.name("accT").value(currentData.accelerationTime)
                            .name("accX").value(currentData.accelerationX)
                            .name("accY").value(currentData.accelerationY)
                            .name("accZ").value(currentData.accelerationZ);
                }
                jsonWriter.endObject();
            }
            catch (IOException e)
            {
                statusText.setText(activity.getResources().getString(R.string.err_write_track_data));
            }
        }
    }

    public void close()
    {
        if (jsonWriter != null) {
            try {
                jsonWriter.endArray();
                Date endDate = new Date();
                jsonWriter.name("end")
                        .beginObject()
                        .name("endT")
                        .value(endDate.getTime())
                        .name("endTFormatted")
                        .value(dateFormat.format(endDate))
                        .endObject()
                        .endObject();
                jsonWriter.close();
                jsonWriter = null;
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

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}

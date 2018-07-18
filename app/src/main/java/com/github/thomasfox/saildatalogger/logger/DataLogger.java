package com.github.thomasfox.saildatalogger.logger;

import android.location.Location;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonWriter;
import android.widget.TextView;

import com.github.thomasfox.saildatalogger.R;
import com.github.thomasfox.saildatalogger.logger.LoggingData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

public class DataLogger {
    /** The current sensor readings. */
    private LoggingData currentData = new LoggingData();

    private AppCompatActivity activity;

    private TextView statusText;

    private JsonWriter jsonWriter;

    public DataLogger(AppCompatActivity activity, TextView statusText, File storageFile)
    {
        this.activity = activity;
        this.statusText = statusText;
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
            jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(storageFile), "UTF-8"));
            jsonWriter.setIndent("  ");
            jsonWriter.beginObject()
                    .name("start")
                    .beginObject()
                    .name("format")
                    .value("v1.1")
                    .name("startT")
                    .value(new Date().getTime())
                    .endObject()
                    .name("track");
            jsonWriter.beginArray();
        } catch (Exception e) {
            statusText.setText(String.format(
                    activity.getResources().getString(R.string.err_write_track_data),
                    e.getMessage()));
        }
    }

    public void setLocation(Location location) {
        currentData.setLocation(location);
        write();
    }

    public void setMagneticField(float[] values) {
        currentData.setMagneticField(values);
    }

    public void setAcceleration(float[] values) {
        currentData.setAcceleration(values);
    }

    private void write() {
        if (jsonWriter != null)
        {
            try {
                jsonWriter.beginObject()
                        .name("locT").value(currentData.locationTime)
                        .name("locAcc").value(currentData.locationAccuracy)
                        .name("locLat").value(currentData.latitude)
                        .name("locLong").value(currentData.longitude)
                        .name("locBear").value(currentData.locationBearing)
                        .name("locVel").value(currentData.locationVelocity)
                        .name("magT").value(currentData.magneticFieldTime)
                        .name("magX").value(currentData.magneticFieldX)
                        .name("magY").value(currentData.magneticFieldY)
                        .name("magZ").value(currentData.magneticFieldZ)
                        .name("accT").value(currentData.accelerationTime)
                        .name("accX").value(currentData.accelerationX)
                        .name("accY").value(currentData.accelerationY)
                        .name("accZ").value(currentData.accelerationZ)
                        .endObject();
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
                jsonWriter.name("end")
                        .beginObject()
                        .name("endT")
                        .value(new Date().getTime())
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
     }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}

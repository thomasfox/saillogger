package com.github.thomasfox.saildatalogger.logger;

import android.location.Location;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonWriter;
import android.widget.TextView;

import com.github.thomasfox.saildatalogger.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataLogger {
    /** The current sensor readings. */
    private LoggingData currentData = new LoggingData();

    private AppCompatActivity activity;

    private TextView statusText;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy' 'HH:mm:ss.SSSZ");

    private JsonWriter jsonWriter;

    public DataLogger(AppCompatActivity activity, TextView statusText, int trackFileNumber)
    {
        this.activity = activity;
        this.statusText = statusText;
        File storageFile = Files.getTrackFile(trackFileNumber);
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
            jsonWriter.setIndent("");
            Date startDate = new Date();
            jsonWriter.beginObject()
                    .name("start")
                    .beginObject()
                    .name("format")
                    .value("v1.3")
                    .name("startT")
                    .value(startDate.getTime())
                    .name("startTFormatted")
                    .value(dateFormat.format(startDate))
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
     }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}

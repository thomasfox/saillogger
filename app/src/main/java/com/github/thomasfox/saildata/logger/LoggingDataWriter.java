package com.github.thomasfox.saildata.logger;

import android.os.Build;
import android.util.JsonWriter;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoggingDataWriter {

    private JsonWriter jsonWriter;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd.MM.yyyy' 'HH:mm:ss.SSSZ",
            Locale.GERMANY);

    public LoggingDataWriter(@NonNull File file) throws FileNotFoundException {
        jsonWriter = new JsonWriter(new OutputStreamWriter(
                new FileOutputStream(file),
                StandardCharsets.UTF_8));
        jsonWriter.setIndent("");
    }

    public void startForAppVersion(String appVersion) throws IOException {
        Date startDate = new Date();
        jsonWriter.beginObject()
                .name("start")
                .beginObject()
                .name("format")
                .value("v1.5")
                .name("loggedBy")
                .value("saildata")
                .name("loggedByVersion")
                .value(appVersion)
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
    }

    public void write(LoggingData toWrite) throws IOException {
        if (jsonWriter == null) {
            throw new IllegalStateException("LoggingDataWriter is already closed");
        }
        jsonWriter.beginObject();
        if (toWrite.hasLocation()) {
            jsonWriter.name("locT").value(toWrite.locationTime)
                    .name("locAcc").value(toWrite.locationAccuracy)
                    .name("locLat").value(toWrite.latitude)
                    .name("locLong").value(toWrite.longitude)
                    .name("locBear").value(toWrite.locationBearing)
                    .name("locVel").value(toWrite.locationVelocity)
                    .name("locAlt").value(toWrite.locationAltitude)
                    .name("locDevT").value(toWrite.locationDeviceTime);
        }
        if (toWrite.hasMagneticField()) {
            jsonWriter.name("magT").value(toWrite.magneticFieldTime)
                    .name("magX").value(toWrite.magneticFieldX)
                    .name("magY").value(toWrite.magneticFieldY)
                    .name("magZ").value(toWrite.magneticFieldZ);
        }
        if (toWrite.hasAcceleration()) {
            jsonWriter.name("accT").value(toWrite.accelerationTime)
                    .name("accX").value(toWrite.accelerationX)
                    .name("accY").value(toWrite.accelerationY)
                    .name("accZ").value(toWrite.accelerationZ);
        }
        jsonWriter.endObject();
    }

    public void close() throws IOException {
        if (jsonWriter != null) {
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
    }
}

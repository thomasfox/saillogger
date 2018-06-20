package com.github.thomasfox.saildatalogger;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.content.Context;
import android.util.JsonWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Date;

import com.github.thomasfox.saildatalogger.R;

class LoggingLocationListener implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2135;

    private TextView statusText;

    private JsonWriter jsonWriter;

    private LocationManager locationManager;

    private AppCompatActivity activity;

    private static final int LOCATION_POLLING_INTERVAL_MILLIS = 500;

    LoggingLocationListener(AppCompatActivity activity, TextView statusText, File storageFile) {
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
            jsonWriter.beginArray();
        } catch (Exception e) {
            statusText.setText(String.format(
                    activity.getResources().getString(R.string.err_write_track_data),
                    e.getMessage()));
        }
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
        else
        {
            registerLocationListener();
        }
    }

    public void onLocationChanged(Location location) {
        statusText.setText(location.toString());

        if (jsonWriter != null)
        {
            LoggingData loggingData = new LoggingData(new Date().getTime())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude());

            try {
                jsonWriter.beginObject()
                        .name("t").value(loggingData.timestamp)
                        .name("lat").value(loggingData.latitude)
                        .name("long").value(loggingData.longitude)
                        .endObject();
            }
            catch (IOException e)
            {
                statusText.setText(activity.getResources().getString(R.string.err_write_track_data));
            }
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public void close()
    {
        try {
            jsonWriter.endArray();
            jsonWriter.close();
            jsonWriter = null;
        }
        catch (IOException e)
        {
            statusText.setText(String.format(
                    activity.getResources().getString(R.string.err_close_track_data_file),
                    e.getMessage()));
        }
        stopLocationListener();
    }

    private void registerLocationListener()
    {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_POLLING_INTERVAL_MILLIS, 0, this);
            statusText.setText(activity.getResources().getString(R.string.info_gps_connected));
        }
        else
        {
            statusText.setText(activity.getResources().getString(R.string.err_gps_permission_denied));
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerLocationListener();
                } else {
                    statusText.setText(activity.getResources().getString(R.string.err_gps_permission_denied));
                }
            }
        }
    }

    private void stopLocationListener()
    {
        locationManager.removeUpdates(this);
    }
}

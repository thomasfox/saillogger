package com.github.thomasfox.saildatalogger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

class LoggingCompassListener implements SensorEventListener {

    private SensorManager sensorManager;

    private DataLogger dataLogger;

    private static final int POLLING_INTERVAL_MILLIS = 500;

    LoggingCompassListener(@NonNull AppCompatActivity activity, @NonNull DataLogger dataLogger) {
        this.dataLogger = dataLogger;
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        registerSensorListener();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            dataLogger.setAcceleration(event.values);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            dataLogger.setMagneticField(event.values);
        }
    }

    public void close()
    {
        stopSensorListener();
    }

    private void registerSensorListener()
    {
        Sensor compass = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, compass, POLLING_INTERVAL_MILLIS);
        Sensor acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, acceleration, POLLING_INTERVAL_MILLIS);
    }

    private void stopSensorListener()
    {
        sensorManager.unregisterListener(this);
    }
}

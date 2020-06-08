package com.github.thomasfox.saildatalogger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.thomasfox.saildatalogger.logger.DataLogger;

class LoggingSensorListener implements SensorEventListener {

    private static final String TAG = "saildatalogger";

    private final SensorManager sensorManager;

    private final DataLogger dataLogger;

    private static final int POLLING_INTERVAL_MICROS = 500000;

    LoggingSensorListener(@NonNull AppCompatActivity activity, @NonNull DataLogger dataLogger) {
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
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                || event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
            dataLogger.setMagneticField(event.values);
        }
    }

    public void close() {
        stopSensorListener();
    }

    private void registerSensorListener()
    {
        Sensor compass = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        if (compass == null)
        {
            Log.i(TAG, "Using calibrated magnetic field sensor.");
            compass = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        else
        {
            Log.i(TAG, "Using uncalibrated magnetic field sensor.");
        }
        sensorManager.registerListener(this, compass, POLLING_INTERVAL_MICROS);
        Sensor acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, acceleration, POLLING_INTERVAL_MICROS);
    }

    private void stopSensorListener()
    {
        sensorManager.unregisterListener(this);
    }
}

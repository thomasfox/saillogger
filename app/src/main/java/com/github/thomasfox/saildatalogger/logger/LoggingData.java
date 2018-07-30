package com.github.thomasfox.saildatalogger.logger;

import android.location.Location;

import java.util.Date;

public class LoggingData {
    // time from GPS signal
    public Long locationTime;

    public Double latitude;

    public Double longitude;

    public Float locationAccuracy;

    public Float locationBearing;

    public Float locationVelocity;

    // device time when GPS data was recorded
    public Long locationDeviceTime;

    // device time when magnetic field was recorded
    public Long magneticFieldTime;

    public Float magneticFieldX;

    public Float magneticFieldY;

    public Float magneticFieldZ;

    // device time when acceleration was recorded
    public Long accelerationTime;

    public Float accelerationX;

    public Float accelerationY;

    public Float accelerationZ;

    public void setLocation(Location location)
    {
        locationTime = location.getTime();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        locationAccuracy = location.getAccuracy();
        locationBearing = location.getBearing();
        locationVelocity = location.getSpeed();
        locationDeviceTime = new Date().getTime();
    }

    public void setMagneticField(float[] magneticField) {
        this.magneticFieldX = magneticField[0];
        this.magneticFieldY = magneticField[1];
        this.magneticFieldZ = magneticField[2];
        this.magneticFieldTime = new Date().getTime();
    }

    public void setAcceleration(float[] acceleration) {
        this.accelerationX = acceleration[0];
        this.accelerationY = acceleration[1];
        this.accelerationZ = acceleration[2];
        this.accelerationTime = new Date().getTime();
    }

    public void reset() {
        locationTime = null;
        latitude = null;
        longitude = null;
        locationAccuracy = null;
        locationBearing = null;
        locationVelocity = null;
        magneticFieldTime = null;
        magneticFieldX = null;
        magneticFieldY = null;
        magneticFieldZ = null;
        accelerationTime = null;
        accelerationX = null;
        accelerationY = null;
        accelerationZ = null;
    }

    public boolean hasLocation() {
        return (locationTime != null
                || latitude != null
                || longitude != null
                || locationAccuracy != null
                || locationBearing != null
                || locationVelocity != null);
    }

    public boolean hasMagneticField() {
        return (magneticFieldTime != null
                || magneticFieldX != null
                || magneticFieldY != null
                || magneticFieldZ != null);
    }

    public boolean hasAcceleration() {
        return (accelerationTime != null
            || accelerationX != null
            || accelerationY != null
            || accelerationZ != null);
    }
}

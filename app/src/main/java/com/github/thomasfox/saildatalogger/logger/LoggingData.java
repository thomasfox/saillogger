package com.github.thomasfox.saildatalogger.logger;

import android.location.Location;

import java.util.Date;

public class LoggingData {
    public Long locationTime;

    public Double latitude;

    public Double longitude;

    public Float locationAccuracy;

    public Float locationBearing;

    public Float locationVelocity;

    public Long magneticFieldTime;

    public Float magneticFieldX;

    public Float magneticFieldY;

    public Float magneticFieldZ;

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
}

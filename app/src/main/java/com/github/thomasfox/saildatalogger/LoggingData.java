package com.github.thomasfox.saildatalogger;

import java.util.Date;

public class LoggingData {
    public Long locationTime;

    public Double latitude;

    public Double longitude;

    public Double locationAccuracy;

    public Long magneticFieldTime;

    public float magneticFieldX;

    public float magneticFieldY;

    public float magneticFieldZ;

    public Long accelerationTime;

    public float accelerationX;

    public float accelerationY;

    public float accelerationZ;

    public LoggingData locationTime(long positionTime) {
        this.locationTime = positionTime;
        return this;
    }

    public LoggingData latitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public LoggingData longitude(double longitude) {
        this.longitude = longitude;
        return this;
    }
    public LoggingData locationAccuracy(double locationAccuracy) {
        this.locationAccuracy = locationAccuracy;
        return this;
    }

    public LoggingData magneticField(float[] magneticField) {
        this.magneticFieldX = magneticField[0];
        this.magneticFieldY = magneticField[1];
        this.magneticFieldZ = magneticField[2];
        this.magneticFieldTime = new Date().getTime();
        return this;
    }

    public LoggingData acceleration(float[] acceleration) {
        this.accelerationX = acceleration[0];
        this.accelerationY = acceleration[1];
        this.accelerationZ = acceleration[2];
        this.accelerationTime = new Date().getTime();
        return this;
    }
}

package com.github.thomasfox.saildatalogger;

public class LoggingData {
    public final long timestamp;

    public double latitude;

    public double longitude;

    LoggingData(long timestamp) {
        this.timestamp = timestamp;
    }

    public LoggingData latitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public LoggingData longitude(double longitude) {
        this.longitude = longitude;
        return this;
    }
}

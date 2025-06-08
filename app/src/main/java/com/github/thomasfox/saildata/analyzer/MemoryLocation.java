package com.github.thomasfox.saildata.analyzer;

import android.location.Location;

import androidx.annotation.NonNull;

import java.util.Date;

public class MemoryLocation {

    // time from GPS signal
    public long locationTimeMillis;

    public double latitude;

    public double longitude;

    public Float locationAccuracy;

    public Float locationBearing;

    public Float locationVelocity;

    public Double locationAltitude;

    // device time when GPS data was recorded
    public Long locationDeviceTimeMillis;

    public MemoryLocation(Location location)
    {
        locationTimeMillis = location.getTime();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        locationAccuracy = location.getAccuracy();
        locationBearing = location.getBearing();
        locationVelocity = location.getSpeed();
        locationAltitude = location.getAltitude();
        locationDeviceTimeMillis = new Date().getTime();
    }

    public long getLocationTimeMillis() {
        return locationTimeMillis;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Float getLocationAccuracy() {
        return locationAccuracy;
    }

    public Float getLocationVelocity() {
        return locationVelocity;
    }

    public Float getLocationBearing() {
        return locationBearing;
    }

    public Double getLocationAltitude() {
        return locationAltitude;
    }

    public Long getLocationDeviceTimeMillis() {
        return locationDeviceTimeMillis;
    }

    /**
     * Returns the distance from the aequator in north direction in meters.
     *
     * @return the north coordinate in meters.
     */
    public double getY()
    {
        return latitude * Constants.EARTH_RADIUS;
    }

    /**
     * Returns the distance from the Greenwich meridian in west direction in meters.
     *
     * @return the west coordinate in meters.
     */
    public double getX()
    {
        return longitude * Math.cos(latitude) * Constants.EARTH_RADIUS;
    }

    /**
     * Calculates the distance between two locations.
     * This uses an approximation which is only valid if the distance between the two points is small
     * as compared to the earth radius.
     *
     * @param other the location to compute the distance to, not null.
     *
     * @return the distance in meters.
     */
    public double approximateDistance(@NonNull MemoryLocation other)
    {
        double xDist = getX() - other.getX();
        double yDist = getY() - other.getY();
        return Math.sqrt(xDist * xDist + yDist * yDist);
    }

    public double approximateVelocity(@NonNull MemoryLocation other)
    {
        return (approximateDistance(other) / (locationTimeMillis - other.locationTimeMillis));
    }
}

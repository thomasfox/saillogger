package com.github.thomasfox.saildata.analyzer;

import android.location.Location;

import androidx.annotation.NonNull;

import java.util.Date;

public class MemoryLocation {

    // time from GPS signal, in milliseconds since 1970
    public long locationTimeMillis;

    // latitude in radians
    public double latitude;

    // longitude in radians
    public double longitude;

    // accuracy in meters
    public Float locationAccuracy;

    // bearing in radians
    public Float locationBearing;

    // speed from GPS signal, in meters per second
    public Float locationVelocity;

    // altitude from GPS signal, in meters
    public Double locationAltitude;

    // device time when GPS data was recorded, in milliseconds since 1970
    public Long deviceTimeMillis;

    public MemoryLocation(Location location)
    {
        locationTimeMillis = location.getTime();
        latitude = location.getLatitude() * Constants.RADIANS_PER_DEGREE;
        longitude = location.getLongitude() * Constants.RADIANS_PER_DEGREE;
        locationAccuracy = location.getAccuracy();
        locationBearing = location.getBearing() * Constants.RADIANS_PER_DEGREE_AS_FLOAT;
        locationVelocity = location.getSpeed();
        locationAltitude = location.getAltitude();
        deviceTimeMillis = new Date().getTime();
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

    public Long getDeviceTimeMillis() {
        return deviceTimeMillis;
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
}

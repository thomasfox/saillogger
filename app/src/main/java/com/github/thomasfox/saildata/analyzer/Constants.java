package com.github.thomasfox.saildata.analyzer;

public class Constants {
    /** radius of the earth in meters. */
    public static final double EARTH_RADIUS = 6371000;

    /** length of a nautical mile in meters. */
    public static final double NAUTICAL_MILE_IN_METERS = 1852;

    /** Number of seconds in one hour. */
    public static final int SECONDS_IN_HOUR = 3600;

    /** Number of milliseconds in one second. */
    public static final int MILLISECONDS_IN_SECONDS = 1000;

    public static final double RADIANS_PER_DEGREE = Math.PI/180d;

    public static final float RADIANS_PER_DEGREE_AS_FLOAT
            = Double.valueOf(Math.PI/360).floatValue();
}

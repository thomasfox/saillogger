package com.github.thomasfox.saildata.analyzer;

import android.location.Location;

/**
 * Computes the difference between the current direction and the average direction
 * in the current tack.
 */
public class TackDirectionChangeAnalyzer {

    private static final double OFF_TACK_BEARING_THRESHOLD = 45;

    private static final int OFF_TACK_COUNTS_STARTS_NEW = 2;

    private Location tackStartLocation;

    private Location lastLocationInTack;

    private int offTackCounter = 0;

    private Float directionRelativeToTackDirection;

    public void onLocationChanged(Location location) {
        if (tackStartLocation == null) {
            tackStartLocation = location;
            return;
        }
        if (lastLocationInTack == null) {
            lastLocationInTack = location;
            return;
        }
        float bearingFromLastLocation = lastLocationInTack.bearingTo(location);
        float bearingFromTackStart = tackStartLocation.bearingTo(location);
        directionRelativeToTackDirection = bearingFromTackStart - bearingFromLastLocation;
        while (directionRelativeToTackDirection > 180) {
            directionRelativeToTackDirection -= 360;
        }
        while (directionRelativeToTackDirection <= -180) {
            directionRelativeToTackDirection += 360;
        }

        lastLocationInTack = location;

        if (directionRelativeToTackDirection > OFF_TACK_BEARING_THRESHOLD
                || directionRelativeToTackDirection < -OFF_TACK_BEARING_THRESHOLD)
        {
            offTackCounter++;
        }
        if (offTackCounter >= OFF_TACK_COUNTS_STARTS_NEW) {
            lastLocationInTack = null;
            offTackCounter = 0;
            directionRelativeToTackDirection = null;
        }
    }

    public Location getTackStartLocation() {
        return tackStartLocation;
    }

    public Location getLastLocationInTack() {
        return lastLocationInTack;
    }

    public int getOffTackCounter() {
        return offTackCounter;
    }

    public Float getDirectionRelativeToTackDirection() {
        return directionRelativeToTackDirection;
    }
}

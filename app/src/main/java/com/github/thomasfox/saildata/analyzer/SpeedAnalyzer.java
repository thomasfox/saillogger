package com.github.thomasfox.saildata.analyzer;

import androidx.annotation.NonNull;

import java.util.Iterator;

public class SpeedAnalyzer
{
    private final MemoryLocationStore memoryLocationStore;

    public SpeedAnalyzer(@NonNull MemoryLocationStore memoryLocationStore) {
        this.memoryLocationStore = memoryLocationStore;
    }

    public double getMaxSpeedAveragedInKnots(double averageMillis)
    {
        double maxAverageVelocityInMetersPerMillis = Double.NaN;
        if (!memoryLocationStore.isLocationTimeMonotonicallyIncreasing())
        {
            return maxAverageVelocityInMetersPerMillis;
        }

        try {
            Iterator<MemoryLocation> secondMemoryLocationsIt
                    = memoryLocationStore.getLocations().iterator();
            MemoryLocation secondLocation = secondMemoryLocationsIt.next();
            MemoryLocation nextSecondLocation = secondMemoryLocationsIt.next();
            for (MemoryLocation firstLocation : memoryLocationStore.getLocations()) {
                if (firstLocation.getLocationTimeMillis() - secondLocation.getLocationTimeMillis() < averageMillis) {
                    // will possibly happen only at start of track
                    continue;
                }
                while (firstLocation.getLocationTimeMillis() - nextSecondLocation.getLocationTimeMillis() >= averageMillis) {
                    secondLocation = nextSecondLocation;
                    if (secondMemoryLocationsIt.hasNext()) {
                        nextSecondLocation = secondMemoryLocationsIt.next();
                    }
                }
                double distance = firstLocation.approximateDistance(secondLocation);
                long elapsedTime = firstLocation.locationTimeMillis - secondLocation.locationTimeMillis;
                double averageVelocityInMetersPerMillis = distance / elapsedTime;
                if (Double.isNaN(maxAverageVelocityInMetersPerMillis)
                        || averageVelocityInMetersPerMillis > maxAverageVelocityInMetersPerMillis)
                {
                    maxAverageVelocityInMetersPerMillis = averageVelocityInMetersPerMillis;
                }
            }
        }
        catch (Exception e)
        {
            maxAverageVelocityInMetersPerMillis = Double.NaN;
        }
        return maxAverageVelocityInMetersPerMillis
                * Constants.SECONDS_IN_HOUR / Constants.NAUTICAL_MILE_IN_METERS * Constants.MILLISECONDS_IN_SECONDS;
    }
}

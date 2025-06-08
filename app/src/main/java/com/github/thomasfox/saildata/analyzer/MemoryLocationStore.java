package com.github.thomasfox.saildata.analyzer;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MemoryLocationStore
{
    private final List<MemoryLocation> locations = new ArrayList<>();

    public void onLocationChanged(Location location) {
        try {
            locations.add(new MemoryLocation(location));
        }
        catch (RuntimeException e)  {
            Log.w(MemoryLocationStore.class.getSimpleName(),
                    "Could not create MemoryLocation from " + location );
        }
    }

    public List<MemoryLocation> getLocations() {
        return locations;
    }

    /**
     * Returns true if any entry in the list has a larger or equal locationTime
     * than all earlier entries in the list.
     *
     * @return true if there is no entry which has a larger index in the list
     *      * but a smaller locationTime than any earlier index in the list
     */
    public boolean isLocationTimeMonotonicallyIncreasing()
    {
        Iterator<MemoryLocation> memoryLocationIterator = locations.iterator();
        if (!memoryLocationIterator.hasNext())
        {
            return true;
        }
        MemoryLocation firstLocation = memoryLocationIterator.next();
        if (!memoryLocationIterator.hasNext())
        {
            return true;
        }
        MemoryLocation secondLocation = memoryLocationIterator.next();
        if (firstLocation.locationTimeMillis > secondLocation.locationTimeMillis)
        {
            return false;
        }
        while (memoryLocationIterator.hasNext())
        {
            firstLocation = secondLocation;
            secondLocation = memoryLocationIterator.next();
            if (firstLocation.locationTimeMillis > secondLocation.locationTimeMillis)
            {
                return false;
            }
        }
        return true;
    }
}

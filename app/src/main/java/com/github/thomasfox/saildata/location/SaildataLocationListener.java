package com.github.thomasfox.saildata.location;

import androidx.annotation.NonNull;
import android.location.Location;

/**
 * Implemented by classes that want to receive location updates.
 */
public interface SaildataLocationListener {

    /**
     * Called when the location has changed.
     *
     * @param location the new location.
     */
    void onLocationChanged(@NonNull Location location);

    /**
     * Called when the permission for location updates has been denied.
     */
    void onPermissionDenied();

    /**
     * Called when the location service has been started.
     */
    void onLocationPollStarted();
}

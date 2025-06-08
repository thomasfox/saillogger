package com.github.thomasfox.saildata.location;

import android.location.Location;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.thomasfox.saildata.analyzer.AnalyzerHub;
import com.github.thomasfox.saildata.logger.DataLogger;
import com.github.thomasfox.saildata.sender.BleSender;

/**
 * Receives location information from the location service and passes the information
 * to the places where location information is needed in the application.
 */
public class LocationListenerHub implements SaildataLocationListener {

    private final AppCompatActivity activity;

    private DataLogger dataLogger;

    private BluetoothLocationDisplayer bluetoothLocationDisplayer;

    private BleSender bleSender;

    private final ScreenLocationDisplayer screenLocationDisplayer;

    private AnalyzerHub analyzerHub;

    private LoggingSensorListener compassListener;

    private FakeLocationProvider fakeLocationProvider;


    public LocationListenerHub(@NonNull AppCompatActivity activity,
                               @NonNull ScreenLocationDisplayer screenLocationDisplayer) {
        this.activity = activity;
        this.screenLocationDisplayer = screenLocationDisplayer;
    }

    public void startLogging(
            @NonNull TextView locationTextView,
            int trackFileNumber,
            @NonNull TextView bleStatusTextView) {
        dataLogger = new DataLogger(activity, locationTextView, trackFileNumber);

        analyzerHub = new AnalyzerHub(activity);
        compassListener = new LoggingSensorListener(activity, dataLogger);
        bleSender = new BleSender(activity, bleStatusTextView);
        bluetoothLocationDisplayer = new BluetoothLocationDisplayer(
                activity,
                bleSender,
                analyzerHub.getTackDirectionChangeAnalyzer());
    }

    public void onLocationChanged(@NonNull Location location) {
        analyzerHub.onLocationChanged(location);
        dataLogger.onLocationChanged(location);
        screenLocationDisplayer.onLocationChanged(location);
        bluetoothLocationDisplayer.onLocationChanged(location);
    }

    public void onPermissionDenied() {
        screenLocationDisplayer.displayPermissionDenied();
    }

    public void onLocationPollStarted() {
        screenLocationDisplayer.displayWaitForFix();
    }

    public void stopLogging()
    {
        compassListener.close();
        compassListener = null;
        dataLogger.close();
        dataLogger = null;
        if (fakeLocationProvider != null) {
            fakeLocationProvider.close();
            fakeLocationProvider = null;
        }
        bluetoothLocationDisplayer.close();
        bluetoothLocationDisplayer = null;
        bleSender.close();
        bleSender = null;
        analyzerHub.close();
        analyzerHub = null;
    }
}

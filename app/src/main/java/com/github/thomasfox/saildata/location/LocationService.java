package com.github.thomasfox.saildata.location;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.github.thomasfox.saildata.R;

public class LocationService extends Service implements LocationListener {

    private static final String LOG_TAG ="saildata:LocService";

    private static final String DEFAULT_IMPORTANCE_CHANNEL_ID = "DefaultImportanceChannel";

    private static final boolean USE_FAKE_LOCATION = false;

    private static boolean notificationChannelCreated = false;

    private LocationManager _locManager;

    private FakeLocationProvider fakeLocationProvider;

    private SaildataLocationListener locationListenerToForwardTo;

    private final IBinder serviceBinder = new LocationBinder();

    private void createNotificationChannel() {
        if (notificationChannelCreated) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_default_importance_name);
            String description = getString(R.string.notification_channel_default_importance_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(
                    DEFAULT_IMPORTANCE_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationChannelCreated = true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        createNotificationChannel();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification =
                new NotificationCompat.Builder(this, DEFAULT_IMPORTANCE_CHANNEL_ID)
                        .setContentTitle(getText(R.string.notification_background_location_title))
                        .setSmallIcon(R.drawable.ic_saildata_foreground)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.notification_ticker_text))
                        .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(300, notification, FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(300, notification);
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        if (USE_FAKE_LOCATION) {
            fakeLocationProvider = new FakeLocationProvider(this);
        }
        else {
            SharedPreferences sharedPreferences
                    = PreferenceManager.getDefaultSharedPreferences(this);
            int pollingIntervalMillis = Integer.parseInt(
                    sharedPreferences.getString("gpsMinUpdateTimeSeconds", "1000"));
            int minUpdateDistanceMeters = Integer.parseInt(
                    sharedPreferences.getString("gpsMinUpdateDistanceMeters", "2"));
            try {
                _locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                _locManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        pollingIntervalMillis,
                        minUpdateDistanceMeters,
                        this);
            } catch (SecurityException e) {
                Log.i(LOG_TAG, "No permission to receive location updates");
                if (locationListenerToForwardTo != null) {
                    locationListenerToForwardTo.onPermissionDenied();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fakeLocationProvider != null) {
            fakeLocationProvider.close();
            fakeLocationProvider = null;
        }
        stopForeground(true);
        if (_locManager != null) {
            _locManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (locationListenerToForwardTo != null) {
            locationListenerToForwardTo.onLocationChanged(location);
        }
    }

    public void registerCallback(@NonNull SaildataLocationListener locationListener) {
        this.locationListenerToForwardTo = locationListener;
        locationListenerToForwardTo.onLocationPollStarted();
    }

    public void clearCallback() {
        this.locationListenerToForwardTo = null;
    }


    public class LocationBinder extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}

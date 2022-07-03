package com.github.thomasfox.saildata.sender;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.thomasfox.saildata.R;

/**
 * Scans bluetooth LE devices and notifies a callback object about found devices.
 * Can display status messages to a TextView, if desired.
 */
public class BleScannerWithStatusView extends ScanCallback {

    private static final String LOG_TAG ="saildata:BLEScanner";

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 9561;

    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 1078;

    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 377;

    private static final long BLUETOOTH_DEVICE_SCAN_PERIOD_MILLIS = 10000;

    private BluetoothLeScanner bluetoothLeScanner;

    private boolean scanning;

    private final Handler handler = new Handler();

    private final TextView messageView;

    private final BleScanCallback bleScanCallback;

    public BleScannerWithStatusView(
            @NonNull TextView messageView,
            @NonNull BleScanCallback bleScanCallback) {
        this.messageView = messageView;
        this.bleScanCallback = bleScanCallback;
    }

    public void scanFailed(String errorMessage) {
        stopScanInternal();
        Log.i(LOG_TAG, errorMessage);
        bleScanCallback.scanFailed(errorMessage);
    }

    public void startScan(Activity activity) {
        askForPermissions(activity);
        if (scanning) {
            Log.i(LOG_TAG, "Scan already running, not starting a new one");
            return;
        }
        scanning = true;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // bluetoothAdapter is null if bluetooth is not supported
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            scanFailed(activity.getString(R.string.bluetooth_not_supported));
            return;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) {
            scanFailed(activity.getString(R.string.bluetooth_not_supported));
            return;
        }

        handler.postDelayed(() -> {
            stopScanInternal();
            Log.i(LOG_TAG, "Stopped bluetooth LE scan");
            messageView.setText(R.string.scan_finished);
            bleScanCallback.scanFinished();
        }, BLUETOOTH_DEVICE_SCAN_PERIOD_MILLIS);

        messageView.setText(R.string.scanning_bluetooth_le_devices);
        Log.i(LOG_TAG, "Started bluetooth LE scan");
        try {
            bluetoothLeScanner.startScan(this);
        }
        catch(SecurityException e) {
            Log.i(LOG_TAG, "SecurityException while calling startScan", e);
            messageView.setText(R.string.bluetooth_not_supported);
        }
    }

    public void stopScan() {
        Log.i(LOG_TAG, "StopScan called");
        if (scanning) {
            messageView.setText(R.string.scan_canceled);
        }
        stopScanInternal();
    }

    public boolean isScanning() {
        return scanning;
    }

    private void stopScanInternal() {
        if (bluetoothLeScanner != null) {
            try {
                bluetoothLeScanner.stopScan(this);
            }
            catch (SecurityException e) {
                Log.w(LOG_TAG, "SecurityException while calling stopScanInternal", e);
                messageView.setText(R.string.bluetooth_not_supported);
            }
        }
        scanning = false;
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        Log.i(LOG_TAG, "Found device with name " + result.getDevice().getName()
                + " and address " + result.getDevice().getAddress());
        bleScanCallback.deviceFound(result.getDevice());
    }

    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        String message = "Scan failed with result " + errorCode;
        Log.i(LOG_TAG, message);
        stopScanInternal();
        bleScanCallback.scanFailed(message);
    }

    private void askForPermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH},
                    MY_PERMISSIONS_REQUEST_BLUETOOTH);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN);
        }
    }
}

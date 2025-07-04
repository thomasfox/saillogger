package com.github.thomasfox.saildata.sender;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.github.thomasfox.saildata.R;

import java.util.List;
import java.util.UUID;

public class BleSender {

    private static final String LOG_TAG ="saildata:BLESender";

    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 3987;

    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN = 3987;

    private static final String SPEED_FIELD_PREFIX = "f1:";

    private static final String BEARING_STRING_FIELD_PREFIX = "f2:";

    private static final String BEARING_BAR_FIELD_PREFIX = "f3:";

    /** The uuid of the service used to send data to the BLE device. */
    public final static UUID SERVICE_UUID =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    /** The uuid of the characteristic used to send data to the BLE device. */
    public final static UUID CHARACTERISTIC_UUID =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private final Activity activity;

    private final TextView statusTextView;

    // true if the device can be discovered but no connection can be made to the device
    private boolean incompatibleDevice = true;

    // not null if connected or while connection go to a bluetooth device
    private BluetoothGatt bluetoothGatt;

    // not null if connected to a bluetooth device
    public BluetoothGattCharacteristic bluetoothGattCharacteristic;

    private boolean shouldBeConnected = false;

    BleConnectionWatchdog bleConnectionWatchdog;

    public BleSender(@NonNull Activity activity, @NonNull TextView statusTextView) {
        this.activity = activity;
        this.statusTextView = statusTextView;

        askForBluetoothPermissions(activity);

        connect(activity);
    }

    static void askForBluetoothPermissions(@NonNull Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN);
        }
    }

    public void connect(@NonNull Activity activity) {

        if (shouldBeConnected) {
            Log.i(LOG_TAG, "Calling connect() while connecting is already in progress, ignoring");
            return;
        }
        shouldBeConnected = true;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String address = prefs.getString("bleDeviceAddress", null);
        if (address == null) {
            Log.i(LOG_TAG, "not connecting, address not set");
            return;
        }
        Log.i(LOG_TAG, "Creating a new connect thread...");
        bleConnectionWatchdog = new BleConnectionWatchdog(this, activity, address);
        bleConnectionWatchdog.start();
    }

    /**
     * Connects to the GATT server on the BLE device and finds the characteristic
     * used for communicating via BLE.
     *
     * @param address The device address of the destination device.
     */
    void connectInternal(@NonNull Activity activity, @NonNull final String address) {
        if (!shouldBeConnected) {
            Log.i(LOG_TAG, "Call to connectInternal when shouldBeConnected is false, ignoring");
            return;
        }
        statusChanged(R.string.status_connecting);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(LOG_TAG,
            "Got Bluetooth adapter, looking for remote device with address " + address);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(LOG_TAG, "Device " + address + " not found.  Unable to connect.");
            statusChanged(R.string.status_device_not_found);
            return;
        }
        Log.d(LOG_TAG, "Found device with address " + address);
        closeCurrentBleConnection();
        try {
            bluetoothGatt = device.connectGatt(activity, false, new SaildataBluetoothGattCallback());
        }
        catch (SecurityException e) {
            statusChanged(R.string.status_permission_denied);
            Log.w(LOG_TAG, "error while connectGatt: " + e);
        }

        Log.d(LOG_TAG, "Started to create a new GATT connection.");
    }

    public boolean isConnected() {
        return bluetoothGattCharacteristic != null;
    }

    void findService(List<BluetoothGattService> serviceList)
    {
        Log.i(LOG_TAG, "Found " + serviceList.size() + " GATT Services");
        if (!shouldBeConnected) {
            Log.i(LOG_TAG, "Call to findService while shouldBeConnected is false, disconnecting");
            closeCurrentBleConnection();
        }
        for (BluetoothGattService service : serviceList)
        {
            if (service.getUuid().toString().equalsIgnoreCase(SERVICE_UUID.toString()))
            {
                Log.i(LOG_TAG, "Found matching BLE service");
                List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
                Log.i(LOG_TAG, "Found " + characteristicList.size() + " characteristics");
                for (BluetoothGattCharacteristic characteristic : characteristicList)
                {
                    if (characteristic.getUuid().toString().equalsIgnoreCase(
                            CHARACTERISTIC_UUID.toString()))
                    {
                        Log.i(LOG_TAG, "Found matching BLE characteristic");
                        try {
                            bluetoothGatt.setCharacteristicNotification(characteristic, true);
                        }
                        catch (SecurityException e) {
                            statusChanged(R.string.status_permission_denied);
                            Log.w(LOG_TAG, "error while setting CharacteristicNotification: " + e);
                        }
                        bluetoothGattCharacteristic = characteristic;
                        incompatibleDevice = false;
                        statusChanged(R.string.status_connected);
                        sendSpeadBearingAndBarIfConnected("--", "--", 0);
                        return;
                    }
                    else
                    {
                        Log.i(LOG_TAG,
                            "Encountered unknown characteristic with UUID "
                                    + characteristic.getUuid().toString());

                    }
                }
            }
            else {
                Log.i(LOG_TAG,
                    "Encountered unknown service with UUID " + service.getUuid().toString());
            }
        }
    }

    public boolean isConnectionAttemptedToIncompatibleDevice() {
        return incompatibleDevice;
    }

    /**
     * Closes the current BLE connection and prohibits any further attempts to reconnect.
     */
    public void close() {
        shouldBeConnected = false;
        closeBleConnectionWatchdog();
        closeCurrentBleConnection();
        Log.i(LOG_TAG, "Done Disconnecting from bluetooth");
        statusChanged(R.string.status_stopped);
    }

    private void closeBleConnectionWatchdog() {
        if (bleConnectionWatchdog != null) {
            bleConnectionWatchdog.close();
            bleConnectionWatchdog = null;
        }
    }

    /**
     * Closes the current BLE connection.
     * Reconnect attempts via the bluetoothConnectThread are not prohibited.
     */
    void closeCurrentBleConnection() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGattCharacteristic = null;
        try {
            bluetoothGatt.close();
        }
        catch (SecurityException e) {
            statusChanged(R.string.status_permission_denied);
            Log.w(LOG_TAG, "error while closing connection: " + e);
        }

        bluetoothGatt = null;
        statusChanged(R.string.status_disconnected);
    }

    public void sendSpeadBearingAndBarIfConnected(
            String speed,
            String bearing,
            Integer bearingBar)
    {
        sendFieldsIfConnected(
                prefixOrNull(SPEED_FIELD_PREFIX , speed),
                prefixOrNull(BEARING_STRING_FIELD_PREFIX, bearing),
                prefixOrNull(BEARING_BAR_FIELD_PREFIX, bearingBar));
    }

    private String prefixOrNull(String prefix, Object value) {
        if (value == null) {
            return null;
        }
        return prefix + value;
    }

    public void sendFieldsIfConnected(String... fieldStrings) {
        StringBuilder valueBuilder = new StringBuilder();
        for (String fieldString : fieldStrings) {
            if (fieldString != null) {
                valueBuilder.append(fieldString);
                valueBuilder.append(';');
            }
        }
        if (valueBuilder.length() > 0) {
            sendRawStringIfConnected(valueBuilder.toString());
        }
    }

    public void sendRawStringIfConnected(String strValue) {
        if (bluetoothGatt != null && bluetoothGattCharacteristic != null) {
            bluetoothGattCharacteristic.setValue(strValue.getBytes());
            try
            {
                bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
            }
            catch (SecurityException e) {
                statusChanged(R.string.status_permission_denied);
                Log.w(LOG_TAG, "error while writing Data: " + e);
            }

            // it seems that the display needs a little time after sending
            // before it can receive the next data packet
            try {
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        else {
            Log.d(LOG_TAG, "not connected, ignoring data " + strValue);
        }
    }

    void statusChanged(int statusTextResourceId) {
        activity.runOnUiThread(() ->
            statusTextView.setText(
                activity.getResources().getString(
                    R.string.status_ble_tag,
                    activity.getResources().getString(statusTextResourceId))));
    }

    private final class SaildataBluetoothGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(
                BluetoothGatt gatt,
                int connectionStatus,
                int newState) {
            if (connectionStatus == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothProfile.STATE_CONNECTED) {
                statusChanged(R.string.status_find_services);
                Log.i(LOG_TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                try {
                    if (bluetoothGatt.discoverServices()) {
                        Log.i(LOG_TAG, "Service discovery started");
                    }
                    else {
                        Log.i(LOG_TAG, "Failed to start service discovery, disconnecting");
                        closeCurrentBleConnection();
                        incompatibleDevice = true;
                    }
                }
                catch (SecurityException e) {
                    statusChanged(R.string.status_permission_denied);
                    Log.w(LOG_TAG, "error while setting closing connection: " + e);
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(LOG_TAG, "Disconnected from GATT Server");
                closeCurrentBleConnection();
            }
            else {
                Log.i(LOG_TAG, "BLE status changed. ConnectionStatus=" + connectionStatus
                        + " NewStatus=" + newState);
                closeCurrentBleConnection();
                incompatibleDevice = true;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(LOG_TAG, "onServicesDiscovered received: " + status);
                findService(gatt.getServices());
            }
            else {
                Log.i(LOG_TAG, "Service discovery status changed. ConnectionStatus=" + status);
                incompatibleDevice = true;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(LOG_TAG, characteristic.getStringValue(0));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i(LOG_TAG, characteristic.getStringValue(0));
        }
    }
}

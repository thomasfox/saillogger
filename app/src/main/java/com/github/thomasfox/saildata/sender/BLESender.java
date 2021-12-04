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

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BLESender {
    private static final String LOG_TAG ="Saildata:BLE";

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 9561;

    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 1078;

    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 377;

    /** The uuid of the service used to send data to the BLE device. */
    public final static UUID SERVICE_UUID =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    /** The uuid of the characteristic used to send data to the BLE device. */
    public final static UUID CHARACTERISTIC_UUID =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private static final long CONNECT_SLEEP_TIME_MILLIS = 5000L;

    // true if the device can be discovered but no connection can be made to the device
    private boolean incompatibleDevice = true;

    // not null if connected or while connection go to a bluetooth device
    private BluetoothGatt bluetoothGatt;

    // not null if connected to a bluetooth device
    public BluetoothGattCharacteristic bluetoothGattCharacteristic;

    BluetoothConnectThread bluetoothConnectThread;

    public BLESender(@NonNull Activity activity) {
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

        connect(activity);
    }

    public void connect(@NonNull Activity activity) {
        if (bluetoothConnectThread != null) {
            Log.i(LOG_TAG, "Calling connect() while connecting is already in progress, ignoring");
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String address = prefs.getString("bleDeviceAddress", null);
        if (address == null) {
            Log.i(LOG_TAG, "not connecting, address not set");
            return;
        }
        bluetoothConnectThread = new BluetoothConnectThread(activity, address);
        bluetoothConnectThread.start();
    }

    public void sendLineIfConnected(String toSend) {
        sendRawIfConnected(toSend + ";");
    }

    public void sendRawIfConnected(String strValue)
    {
        if (bluetoothGatt != null && bluetoothGattCharacteristic != null) {
            bluetoothGattCharacteristic.setValue(strValue.getBytes());
            bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
        }
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     */
    public void connect(@NonNull Activity activity, @NonNull final String address) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(LOG_TAG, "Device not found.  Unable to connect.");
            return;
        }
        if (bluetoothGatt != null)
        {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        bluetoothGatt = device.connectGatt(activity, false, new SaildataBluetoothGattCallback());

        Log.d(LOG_TAG, "Started to create a new connection.");
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGattCharacteristic = null;
        bluetoothGatt.close();
        bluetoothGatt = null;
        Log.i(LOG_TAG, "Done Disconnecting from bluetooth");
    }

    public void findService(List<BluetoothGattService> serviceList)
    {
        Log.i(LOG_TAG, "Found " + serviceList.size() + " GATT Services");
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
                        bluetoothGatt.setCharacteristicNotification(characteristic, true);
                        bluetoothGattCharacteristic = characteristic;
                        sendLineIfConnected("123");
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

    private class BluetoothConnectThread extends Thread {

        private final Activity activity;

        private final String address;

        public BluetoothConnectThread(Activity activity, String address) {
            this.activity =activity;
            this.address = address;
        }

        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    if (bluetoothGattCharacteristic != null) {
                        return;
                    }
                    connect(activity, address);
                    return;
                }
                catch (RuntimeException connectException) {
                    close();
                }

                try  {
                    Thread.sleep(CONNECT_SLEEP_TIME_MILLIS);
                }
                catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                }
                catch(Exception ignored) {
                }
            }
        }
    }

    private final class SaildataBluetoothGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(
                BluetoothGatt gatt,
                int connectionStatus,
                int newState) {
            if (connectionStatus == BluetoothGatt.GATT_SUCCESS)
            {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(LOG_TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    if (bluetoothGatt.discoverServices()) {
                        Log.i(LOG_TAG, "Service discovery started");
                    }
                    else
                    {
                        Log.i(LOG_TAG, "Failed to start service discovery, disconnecting");
                        bluetoothGatt.close();
                        bluetoothGatt = null;
                        incompatibleDevice = true;
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                }
            }
            else
            {
                Log.i(LOG_TAG, "BLE status changed. ConnectionStatus=" + connectionStatus
                        + " NewStatus=" + newState);
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

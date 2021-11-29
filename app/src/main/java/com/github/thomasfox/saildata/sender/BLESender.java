package com.github.thomasfox.saildata.sender;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BLESender {
    private static final String LOG_TAG ="Saildata:BLE";

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 9561;

    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 1078;

    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 377;

    private static final String DEVICE_NAME = "HMSoft";

    private static final long CONNECT_SLEEP_TIME_MILLIS = 5000L;

    private BluetoothDevice displayDevice;

    // not null if connected to a bluetooth device
    private BluetoothSocket displaySocket;

    // not null if connected to a bluetooth device
    private OutputStream displayOutputStream;

    // not null if connected to a bluetooth device
    private InputStream displayInputStream;

    BluetoothConnectThread bluetoothConnectThread;

    public BLESender(@NonNull Activity activity, @NonNull TextView statusTextView) {
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

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice pairedDevice : pairedDevices) {
            String name = pairedDevice.getName();
            statusTextView.setText("Found paired bluetooth device with name " + name);
            if (DEVICE_NAME.equals(name))
            {
                displayDevice = pairedDevice;
                connect();
                Log.i(LOG_TAG, "Connected to bluetooth device " + name);
                break;
            }
        }
        if (displayDevice == null)
        {
            Log.i(LOG_TAG, "No bonded device found, scanning available devices");
            bluetoothAdapter.getBluetoothLeScanner().startScan(new MyScanCallback());
        }
    }

    public void connect() {
        if (bluetoothConnectThread != null && !bluetoothConnectThread.isConnected()) {
            Log.i(LOG_TAG, "Calling connect() while connecting is already in progress, ignoring");
            return;
        }
        bluetoothConnectThread = new BluetoothConnectThread(displayDevice);
        bluetoothConnectThread.start();
    }

    private void connected(BluetoothSocket bluetoothSocket) {
        if (this.displaySocket != null && this.displaySocket != bluetoothSocket) {
            disconnect();
        }
        this.displaySocket = bluetoothSocket;
        try {
            this.displayOutputStream = bluetoothSocket.getOutputStream();
            this.displayInputStream = bluetoothSocket.getInputStream();
            sendLineIfConnected("999");
        }
        catch (IOException e) {
            Log.w(LOG_TAG, "Could not get output stream from bluetooth socket, disconnecting", e);
            disconnect();
        }
    }

    private void disconnect()
    {
        Log.i(LOG_TAG, "Disconnecting from bluetooth...");
        try {
            displayOutputStream.close();
        }
        catch (IOException e) {
            Log.w(LOG_TAG, "Could not close display output stream", e);
        }
        finally {
            displayOutputStream = null;
        }
        try {
            displayInputStream.close();
        }
        catch (IOException e) {
            Log.w(LOG_TAG, "Could not close display input stream", e);
        }
        finally {
            displayInputStream = null;
        }
        try {
            displaySocket.close();
        } catch (IOException e) {
            Log.w(LOG_TAG, "Could not close bluetooth socket", e);
        }
        finally {
            displaySocket = null;
        }
        Log.i(LOG_TAG, "Done Disconnecting from bluetooth");
    }

    public void sendRawIfConnected(String toSend) {
        if (displayOutputStream != null) {
            try {
                displayOutputStream.write(toSend.getBytes("ISO-8859-1"));
            }
            catch (IOException | RuntimeException e) {
                Log.w(LOG_TAG, "Could not send message " + toSend, e);
                disconnect();
                connect();
            }
        }
    }

    public void sendLineIfConnected(String toSend) {
        sendRawIfConnected(toSend + ";");
    }

    public void receiveAndLogIfConnected() {
        if (displayInputStream != null) {
            try {
                StringBuilder received = new StringBuilder();
                while (displayInputStream.available() > 0) {
                    received.append(new String(new byte[]{(byte) displayInputStream.read()}, "ISO-8859-1"));
                }
                if (received.length() > 0) {
                    Log.i(LOG_TAG, "Received: " + received);
                }
            }
            catch (IOException  | RuntimeException e) {
                Log.w(LOG_TAG, "Could not read from bluetooth", e);
            }
        }

    }


    private class BluetoothConnectThread extends Thread {
        private final BluetoothSocket bluetoothSocket;

        private boolean connected;

        public BluetoothConnectThread(BluetoothDevice device) {
            Log.i(LOG_TAG,"Creating bluetooth socket...");
            BluetoothSocket tmp = null;

            try {
               tmp = device.createRfcommSocketToServiceRecord(
                       UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            }
            catch (IOException e) {
                Log.e(LOG_TAG, "Could not create bluetooth socket", e);
            }
            bluetoothSocket = tmp;
            Log.i(LOG_TAG,"Done creating bluetooth socket");
        }

        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Log.i(LOG_TAG,"Connecting bluetooth socket...");
                    bluetoothSocket.connect();
                    Log.i(LOG_TAG,"Done connecting bluetooth socket");
                    connected = true;
                    connected(bluetoothSocket);
                    return;
                }
                catch (IOException | RuntimeException connectException) {
                    try {
                        bluetoothSocket.close();
                    }
                    catch (IOException closeException) {
                        Log.e(LOG_TAG, "Could not close the client socket while connecting", closeException);
                    }
                }

                try  {
                    Thread.sleep(CONNECT_SLEEP_TIME_MILLIS);
                }
                catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                }
                catch(Exception e) {
                }
            }
        }

        public synchronized boolean isConnected() {
            return connected;
        }
    }

    class MyScanCallback extends ScanCallback
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);
            ScanRecord scanRecord = result.getScanRecord();

            if (scanRecord != null && displayDevice == null) {
                Log.i(LOG_TAG, String.format("Device Result: %s %s", result.getDevice().getName(), result.toString()));
                if (result.getDevice().getName().equals(DEVICE_NAME))
                {
                    displayDevice = result.getDevice();
                    connect();
                    Log.i(LOG_TAG, "Connected to bluetooth device " + result.getDevice().getName());
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            for (ScanResult result : results) {
                Log.i(LOG_TAG, String.format("ScanResult - Results: %s", result.toString()));
            }
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            Log.e(LOG_TAG, String.format("Scan Failed. Error Code: %s", errorCode));
        }
    }
}

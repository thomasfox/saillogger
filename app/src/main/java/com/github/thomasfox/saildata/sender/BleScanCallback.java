package com.github.thomasfox.saildata.sender;

import android.bluetooth.BluetoothDevice;

public interface BleScanCallback {
    void deviceFound(BluetoothDevice bluetoothDevice);

    void scanFinished();

    void scanFailed(String errorMessage);
}

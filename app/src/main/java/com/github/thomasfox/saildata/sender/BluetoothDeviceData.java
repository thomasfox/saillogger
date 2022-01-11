package com.github.thomasfox.saildata.sender;

import android.bluetooth.BluetoothDevice;

import java.util.Objects;

/**
 * Contains information about one discovered bluetooth device.
 */
public class BluetoothDeviceData {

    private String name;
    private String macAddress;

    public BluetoothDeviceData(BluetoothDevice bluetoothDevice) {
        this.name = bluetoothDevice.getName();
        this.macAddress = bluetoothDevice.getAddress();
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BluetoothDeviceData that = (BluetoothDeviceData) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(macAddress, that.macAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, macAddress);
    }
}

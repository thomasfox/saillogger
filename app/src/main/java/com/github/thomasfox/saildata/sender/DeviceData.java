package com.github.thomasfox.saildata.sender;

import android.bluetooth.BluetoothDevice;

import java.util.Objects;

public class DeviceData {
    private String name;
    private String macAddress;

    public DeviceData(BluetoothDevice bluetoothDevice) {
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
        DeviceData that = (DeviceData) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(macAddress, that.macAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, macAddress);
    }
}

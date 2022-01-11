package com.github.thomasfox.saildata.sender;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.thomasfox.saildata.R;

import java.util.ArrayList;

public class BluetoothDeviceList extends BaseAdapter {

    private static final String LOG_TAG ="Saildata:BLDeviceList";

    private ArrayList<BluetoothDeviceData> devices = new ArrayList<>();

    private LayoutInflater layoutInflater;

    private boolean scanFailed = false;

    public BluetoothDeviceList(@NonNull LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
    }

    public void addDevice(BluetoothDevice device) {
        BluetoothDeviceData toAdd = new BluetoothDeviceData(device);
        if (!devices.contains(toAdd)) {
            devices.add(toAdd);
        }
        notifyDataSetChanged();
    }

    public BluetoothDeviceData getDevice(int position) {
        return devices.get(position);
    }

    public void clear() {
        devices.clear();
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void notifyScanFailed() {
        this.scanFailed = true;
    }

    public void resetScanFailed() {
        this.scanFailed = false;
    }

    public boolean isScanFailed() {
        return scanFailed;
    }

    @Override
    public View getView(int position, View viewToReuse, ViewGroup viewGroup) {
        View view;
        if (viewToReuse == null) {
            view = layoutInflater.inflate(R.layout.listitem_bluetoothdevice, null);
        } else {
            view = viewToReuse;
        }
        TextView deviceAddressView = view.findViewById(R.id.bluetooth_device_address);
        TextView deviceNameView = view.findViewById(R.id.bluetooth_device_name);

        BluetoothDeviceData device = devices.get(position);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0) {
            deviceNameView.setText(deviceName);
        }
        else {
            deviceNameView.setText(R.string.unknown_device);
        }

        deviceAddressView.setText(device.getMacAddress());

        return view;
    }

}


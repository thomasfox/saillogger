package com.github.thomasfox.saildata;

import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.github.thomasfox.saildata.sender.BleScanCallback;
import com.github.thomasfox.saildata.sender.BleScannerWithStatusView;
import com.github.thomasfox.saildata.sender.BluetoothDeviceList;
import com.github.thomasfox.saildata.sender.DeviceData;

public class BluetoothLeScanActivity extends AppCompatActivity
        implements BleScanCallback, AdapterView.OnItemClickListener {

    private static final String LOG_TAG ="Saildata:BLEScanAct";

    private BluetoothDeviceList bluetoothDeviceList;

    private BleScannerWithStatusView bleScanner;

    private TextView messageView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetoothlescan);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.bluetooth_le_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        messageView = findViewById(R.id.scanBluetoothDeviceMessage);
        bluetoothDeviceList = new BluetoothDeviceList(this.getLayoutInflater());
        bleScanner = new BleScannerWithStatusView(messageView, this);

        setSupportActionBar((Toolbar) findViewById(R.id.mainToolbar));
        getSupportActionBar().setTitle(
                getResources().getString(R.string.app_name) + " "
                        + getResources().getString(R.string.app_version));
        ListView deviceListView = findViewById(R.id.scanBluetoothDeviceList);
        deviceListView.setAdapter(bluetoothDeviceList);
        deviceListView.setOnItemClickListener(this);
        bleScanner.startScan(this);
    }

    public void toggleScanStatus(View view) {
        if (bleScanner.isScanning()) {
            bleScanner.stopScan();
            ((Button) findViewById(R.id.scanStatusToggleButton)).setText(R.string.button_start_scan);
        }
        else {
            bleScanner.startScan(this);
            ((Button) findViewById(R.id.scanStatusToggleButton)).setText(R.string.button_stop_scan);
        }
    }

    public void done(View view) {
        bleScanner.stopScan();
        finish();
    }

    @Override
    public void deviceFound(BluetoothDevice bluetoothDevice) {
        bluetoothDeviceList.addDevice(bluetoothDevice);
    }

    @Override
    public void scanFinished() {
        ((Button) findViewById(R.id.scanStatusToggleButton)).setText(R.string.button_start_scan);
    }

    @Override
    public void scanFailed(String errorMessage) {
        ((Button) findViewById(R.id.scanStatusToggleButton)).setText(R.string.button_start_scan);
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
        bleScanner.stopScan();
        DeviceData device = bluetoothDeviceList.getDevice(position);
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("bleDeviceAddress", device.getMacAddress())
                .putString("bleDeviceName", device.getName())
                .commit();
        Log.i(LOG_TAG, "Selected device with name " + device.getName() + " and address " + device.getMacAddress());
        finish();
    }
}

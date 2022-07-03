package com.github.thomasfox.saildata.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.github.thomasfox.saildata.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    public void ok(View view) {
        finish();
    }

    public void scanBluetoothLeDevices(View view) {
        final Intent intent = new Intent(this, BluetoothLeScanActivity.class);
        startActivity(intent);
    }
}

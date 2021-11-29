package com.github.thomasfox.saildata;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String SETTINGS_KEY_RECORD_VIDEO = "recordVideo";

    public static final String SETTINGS_KEY_DIM_SCREEN_WHILE_LOGGING = "dimScreenWhileLogging";

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

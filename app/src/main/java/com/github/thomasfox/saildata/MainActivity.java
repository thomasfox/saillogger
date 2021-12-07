package com.github.thomasfox.saildata;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.thomasfox.saildata.screen.BrightnessListener;
import com.github.thomasfox.saildata.screen.ScreenManager;
import com.github.thomasfox.saildata.sender.BLESender;


public class MainActivity extends AppCompatActivity implements BrightnessListener {

    private static final String LOG_TAG ="Saildata:Main";

    private TextView statusTextView;

    private TextView speedTextView;

    private TextView bearingTextView;

    private ScreenManager screenManager;

    private EnableLoggingClickListener enableLoggingClickListener;

    private ToggleButton enableLoggingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusText);
        speedTextView = findViewById(R.id.speedText);
        speedTextView.setText(getResources().getString(R.string.speed_no_value_text));
        bearingTextView = findViewById(R.id.bearingText);
        bearingTextView.setText(getResources().getString(R.string.bearing_no_value_text));
        screenManager = new ScreenManager(this);
        screenManager.registerBrightnessListener(this);

        enableLoggingClickListener = new EnableLoggingClickListener(
                statusTextView,
                speedTextView,
                bearingTextView,
                this);
        enableLoggingButton = findViewById(R.id.enableLoggingButton);
        enableLoggingButton.setOnClickListener(enableLoggingClickListener);

        ToggleButton dimScreenButton = findViewById(R.id.dimScreenButton);
        dimScreenButton.setOnClickListener(new DimScreenClickListener(this));

        setSupportActionBar(findViewById(R.id.mainToolbar));
        getSupportActionBar().setTitle(
                getResources().getString(R.string.app_name) + " "
                        + getResources().getString(R.string.app_version));
 //       new BluetoothTester(this, statusTextView).start();
    }

    public ScreenManager getScreenManager() {
        return screenManager;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.close:
                finish();
                return true;
            case R.id.settings:
                settings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void settings()
    {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    @Override
    public void brightnessChanged(boolean systemBrightnessRestored) {
        ToggleButton dimScreenButton = findViewById(R.id.dimScreenButton);
        if (dimScreenButton.isChecked() == systemBrightnessRestored) {
            dimScreenButton.setChecked(!systemBrightnessRestored);
        }
    }

    public EnableLoggingClickListener getEnableLoggingClickListener() {
        return enableLoggingClickListener;
    }

    public ToggleButton getEnableLoggingButton() {
        return enableLoggingButton;
    }

    private static class BluetoothTester extends Thread
    {
        Activity activity;
        TextView statusTextView;
        BLESender bluetoothSender;


        public BluetoothTester(Activity activity, TextView statusTextView) {
            this.activity = activity;
            this.statusTextView = statusTextView;
            bluetoothSender = new BLESender(activity);
        }

        @Override
        public void run()
        {
            for (int i=0; i < 99; i++) {
                Log.i(LOG_TAG, "Sending to Bluetooth: " + i);
                bluetoothSender.sendLineIfConnected(Integer.toString(i));
//                Log.i(LOG_TAG, "Reading from Bluetooth...");
//                bluetoothSender.receiveAndLogIfConnected();
                Log.i(LOG_TAG, "done bluetoothing");
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}

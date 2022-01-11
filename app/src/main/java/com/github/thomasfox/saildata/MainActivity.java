package com.github.thomasfox.saildata;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.thomasfox.saildata.screen.BrightnessListener;
import com.github.thomasfox.saildata.screen.ScreenManager;


public class MainActivity extends AppCompatActivity implements BrightnessListener {

    private static final String LOG_TAG ="Saildata:Main";

    private TextView locationTextView;

    private TextView gpsStatusTextView;

    private TextView bleStatusTextView;

    private TextView speedTextView;

    private TextView bearingTextView;

    private ScreenManager screenManager;

    private StartStopLoggingClickListener locationClickListener;

    private ToggleButton enableLoggingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.locationText);
        gpsStatusTextView = findViewById(R.id.statusGpsText);
        bleStatusTextView = findViewById(R.id.statusBleText);
        speedTextView = findViewById(R.id.speedText);
        speedTextView.setText(getResources().getString(R.string.speed_no_value_text));
        bearingTextView = findViewById(R.id.bearingText);
        bearingTextView.setText(getResources().getString(R.string.bearing_no_value_text));
        screenManager = new ScreenManager(this);
        screenManager.registerBrightnessListener(this);

        locationClickListener = new StartStopLoggingClickListener(
                locationTextView,
                gpsStatusTextView,
                bleStatusTextView,
                speedTextView,
                bearingTextView,
                this);
        enableLoggingButton = findViewById(R.id.enableLoggingButton);
        enableLoggingButton.setOnClickListener(locationClickListener);

        ToggleButton dimScreenButton = findViewById(R.id.dimScreenButton);
        dimScreenButton.setOnClickListener(new DimScreenClickListener(this));

        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(
                getResources().getString(R.string.app_name) + " "
                        + getResources().getString(R.string.app_version));
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
        if (item.getItemId() == R.id.close) {
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.settings) {
            settings();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    public StartStopLoggingClickListener getLocationClickListener() {
        return locationClickListener;
    }

    public ToggleButton getEnableLoggingButton() {
        return enableLoggingButton;
    }
}

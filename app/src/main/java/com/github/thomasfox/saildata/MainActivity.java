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

import com.github.thomasfox.saildata.location.ScreenLocationDisplayer;
import com.github.thomasfox.saildata.screen.BrightnessListener;
import com.github.thomasfox.saildata.screen.ScreenManager;

public class MainActivity extends AppCompatActivity implements BrightnessListener {

    private ScreenManager screenManager;

    private StartStopLoggingClickListener startStopLoggingClickListener;

    private ScreenLocationDisplayer screenLocationDisplayer;

    private ToggleButton enableLoggingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView locationTextView = findViewById(R.id.locationText);
        TextView gpsStatusTextView = findViewById(R.id.statusGpsText);
        TextView bleStatusTextView = findViewById(R.id.statusBleText);
        TextView speedTextView = findViewById(R.id.speedText);
        speedTextView.setText(getResources().getString(R.string.speed_no_value_text));
        TextView bearingTextView = findViewById(R.id.bearingText);
        bearingTextView.setText(getResources().getString(R.string.bearing_no_value_text));
        screenManager = new ScreenManager(this);
        screenManager.registerBrightnessListener(this);

        screenLocationDisplayer = new ScreenLocationDisplayer(
                this,
                gpsStatusTextView,
                locationTextView,
                speedTextView,
                bearingTextView);

        startStopLoggingClickListener = new StartStopLoggingClickListener(
                locationTextView,
                bleStatusTextView,
                screenLocationDisplayer,
                this);
        enableLoggingButton = findViewById(R.id.enableLoggingButton);
        enableLoggingButton.setOnClickListener(startStopLoggingClickListener);

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

    public StartStopLoggingClickListener getStartStopLoggingClickListener() {
        return startStopLoggingClickListener;
    }

    public ToggleButton getEnableLoggingButton() {
        return enableLoggingButton;
    }
}

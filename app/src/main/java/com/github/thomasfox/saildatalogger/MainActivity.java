package com.github.thomasfox.saildatalogger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ToggleButton;
import android.widget.TextView;

import com.github.thomasfox.saildatalogger.screen.BrightnessListener;
import com.github.thomasfox.saildatalogger.screen.ScreenManager;

public class MainActivity extends AppCompatActivity implements BrightnessListener {

    private TextView statusText;

    private ScreenManager screenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        screenManager = new ScreenManager(this);
        screenManager.registerBrightnessListener(this);

        ToggleButton enableLoggingButton = findViewById(R.id.enableLoggingButton);
        enableLoggingButton.setOnClickListener(new EnableLoggingClickListener(statusText,this));

        ToggleButton dimScreenButton = findViewById(R.id.dimScreenButton);
        dimScreenButton.setOnClickListener(new DimScreenClickListener(this));

        setSupportActionBar((Toolbar) findViewById(R.id.mainToolbar));
        getSupportActionBar().setTitle(
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
}

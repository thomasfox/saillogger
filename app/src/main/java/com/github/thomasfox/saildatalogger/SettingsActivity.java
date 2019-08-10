package com.github.thomasfox.saildatalogger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import com.github.thomasfox.saildatalogger.state.Settings;

public class SettingsActivity extends AppCompatActivity {

    private CheckBox recordVideoCheckbox;

    private CheckBox dimScreenCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        recordVideoCheckbox = findViewById(R.id.checkbox_record_video);
        recordVideoCheckbox.setChecked(Settings.recordVideo);
        dimScreenCheckbox = findViewById(R.id.checkbox_dim_screen);
        dimScreenCheckbox.setChecked(Settings.dimScreen);
    }

    public void ok(View view)
    {
        Settings.recordVideo = recordVideoCheckbox.isChecked();
        Settings.dimScreen = dimScreenCheckbox.isChecked();
        finish();
    }
}

package com.github.thomasfox.saildata;

import android.view.View;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.github.thomasfox.saildata.screen.ScreenManager;


class DimScreenClickListener implements View.OnClickListener {

    private final AppCompatActivity activity;

    private final ScreenManager screenManager;

    DimScreenClickListener(MainActivity activity) {
        this.activity = activity;
        this.screenManager = activity.getScreenManager();
    }

    @Override
    public void onClick(View view) {
        boolean dimScreen = ((ToggleButton) view).isChecked();
        if (dimScreen) {
            screenManager.minimizeBrightness();
        } else {
            screenManager.restoreSystemBrightness();
       }
    }
}

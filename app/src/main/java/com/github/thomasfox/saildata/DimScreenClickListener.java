package com.github.thomasfox.saildata;

import android.view.View;
import android.widget.ToggleButton;

import com.github.thomasfox.saildata.screen.ScreenManager;

class DimScreenClickListener implements View.OnClickListener {

    private final ScreenManager screenManager;

    DimScreenClickListener(ScreenManager screenManager) {
        this.screenManager = screenManager;
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

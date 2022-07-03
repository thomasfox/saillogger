package com.github.thomasfox.saildata.ui.main;

import android.view.View;
import android.widget.ToggleButton;

import com.github.thomasfox.saildata.screen.ScreenManager;

/**
 * A click listener which handles clicks on the "dim screen" button.
 * If the button is clicked, it toggles the brightness from normal brightness to dimmed,
 * and vive versa.
 */
public class DimScreenClickListener implements View.OnClickListener {

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

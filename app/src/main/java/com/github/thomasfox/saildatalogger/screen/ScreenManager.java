package com.github.thomasfox.saildatalogger.screen;

import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class ScreenManager {

    private Float oldBrightness;

    private final AppCompatActivity activity;

    private List<BrightnessListener> brightnessListenerList = new ArrayList<>();

    public ScreenManager(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void minimizeBrightness()
    {
        if (oldBrightness != null) {
            return;
        }
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        oldBrightness = lp.screenBrightness;
        lp.screenBrightness = 0.01f;
        activity.getWindow().setAttributes(lp);
        for (BrightnessListener brightnessListener : brightnessListenerList) {
            brightnessListener.brightnessChanged(false);
        }
    }

    public void restoreSystemBrightness() {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        if (oldBrightness != null) {
            lp.screenBrightness = oldBrightness;
        }
        activity.getWindow().setAttributes(lp);
        oldBrightness = null;
        for (BrightnessListener brightnessListener : brightnessListenerList) {
            brightnessListener.brightnessChanged(true);
        }
    }

    public void allowScreenOff() {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void disableScreenOff() {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void registerBrightnessListener(BrightnessListener brightnessListener) {
        brightnessListenerList.add(brightnessListener);
    }
}

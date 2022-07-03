package com.github.thomasfox.saildata.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.github.thomasfox.saildata.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onStart() {
        super.onStart();
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.settings);
    }

}


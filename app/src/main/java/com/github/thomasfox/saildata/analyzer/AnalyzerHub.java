package com.github.thomasfox.saildata.analyzer;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.thomasfox.saildata.ui.fragment.TrackStatisticsDialogFragment;

public class AnalyzerHub {

    private final AppCompatActivity activity;

    private final TackDirectionChangeAnalyzer tackDirectionChangeAnalyzer
            = new TackDirectionChangeAnalyzer();

    private final MemoryLocationStore memoryLocationStore = new MemoryLocationStore();

    private final SpeedAnalyzer speedAnalyzer = new SpeedAnalyzer(memoryLocationStore);

    public AnalyzerHub(@NonNull AppCompatActivity activity) {
        this.activity = activity;
    }

    public void onLocationChanged(Location location) {
        tackDirectionChangeAnalyzer.onLocationChanged(location);
        memoryLocationStore.onLocationChanged(location);
    }

    public TackDirectionChangeAnalyzer getTackDirectionChangeAnalyzer() {
        return tackDirectionChangeAnalyzer;
    }

    public void close()
    {
        TrackStatisticsDialogFragment dialogFragment = new TrackStatisticsDialogFragment();
        dialogFragment.showFragment(
                speedAnalyzer.getMaxSpeedAveragedInKnots(2000),
                speedAnalyzer.getMaxSpeedAveragedInKnots(5000),
                speedAnalyzer.getMaxSpeedAveragedInKnots(10000),
                activity.getFragmentManager());
    }

}

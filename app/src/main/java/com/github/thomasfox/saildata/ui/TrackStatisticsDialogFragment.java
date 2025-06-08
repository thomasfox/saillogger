package com.github.thomasfox.saildata.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.github.thomasfox.saildata.R;

public class TrackStatisticsDialogFragment extends DialogFragment {

    public static final String TWO_SECONDS_VELOCITY_AVERAGE_BUNDLE_KEY = "average.twoseconds";
    public static final String FIVE_SECONDS_VELOCITY_AVERAGE_BUNDLE_KEY = "average.fiveseconds";
    public static final String TEN_SECONDS_VELOCITY_AVERAGE_BUNDLE_KEY = "average.tenseconds";


    public void showFragment(
            double twoSecondsAverage,
            double fiveSecondsAverage,
            double tenSecondsAverage,
            FragmentManager fragmentManager)
    {
        Bundle bundle = new Bundle();
        bundle.putString(
                TWO_SECONDS_VELOCITY_AVERAGE_BUNDLE_KEY,
                String.format("%.1f kn", twoSecondsAverage));
        bundle.putString(
                FIVE_SECONDS_VELOCITY_AVERAGE_BUNDLE_KEY,
                String.format("%.1f kn", fiveSecondsAverage));
        bundle.putString(
                TEN_SECONDS_VELOCITY_AVERAGE_BUNDLE_KEY,
                String.format("%.1f kn", tenSecondsAverage));
        setArguments(bundle);
        show(fragmentManager, "trackStatisticsDialog");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle bundle = this.getArguments();
        String twoSecondsAverage = bundle.getString(TWO_SECONDS_VELOCITY_AVERAGE_BUNDLE_KEY);
        String fiveSecondsAverage = bundle.getString(FIVE_SECONDS_VELOCITY_AVERAGE_BUNDLE_KEY);
        String tenSecondsAverage = bundle.getString(TEN_SECONDS_VELOCITY_AVERAGE_BUNDLE_KEY);
        builder.setMessage(String.format(
                        getActivity().getResources().getString(R.string.info_track_statistics),
                        twoSecondsAverage, fiveSecondsAverage, tenSecondsAverage))
                .setPositiveButton(
                        R.string.button_ok,
                        (dialog, id) -> {});
        return builder.create();
    }
}

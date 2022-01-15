package com.github.thomasfox.saildata;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class StopLoggingDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.message_stop_logging)
                .setPositiveButton(
                        R.string.button_stop_logging,
                        (dialog, id) -> ((MainActivity) getActivity())
                                .getStartStopLoggingClickListener()
                                .stopLogging())
                .setNegativeButton(
                        R.string.button_continue_logging,
                        (dialog, id) -> ((MainActivity) getActivity())
                                .getEnableLoggingButton()
                                .setChecked(true));
        return builder.create();
    }
}

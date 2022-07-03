package com.github.thomasfox.saildata.ui.main;

import android.app.AlertDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;

import com.github.thomasfox.saildata.R;

/**
 * Asks the user whether he really wants to stop logging.
 * If yes, logging is stopped.
 * If no, logging is continued and the state of the "start/stop logging" button is changed
 * to reflect that logging is continued.
 */
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

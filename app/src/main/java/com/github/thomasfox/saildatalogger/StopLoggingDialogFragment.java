package com.github.thomasfox.saildatalogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class StopLoggingDialogFragment extends DialogFragment {

    private static final String TAG = "saildatalogger";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.message_stop_logging)
                .setPositiveButton(R.string.button_stop_logging, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((MainActivity) getActivity()).getEnableLoggingClickListener().stopLogging();
                    }
                })
                .setNegativeButton(R.string.button_continue_logging, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ((MainActivity) getActivity()).getEnableLoggingButton().setChecked(true);
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

package com.github.thomasfox.saildata.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.github.thomasfox.saildata.R;

public class WroteFileDialogFragment extends DialogFragment {

    public static final String FILE_LOCATION_BUNDLE_KEY = "file.location";

    public void showFragment(String absolutePath, FragmentManager fragmentManager) {
        Bundle bundle = new Bundle();
        bundle.putString(
                WroteFileDialogFragment.FILE_LOCATION_BUNDLE_KEY,
                absolutePath);
        setArguments(bundle);
        show(fragmentManager, "wroteFileDialog");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle bundle = this.getArguments();
        String fileLocation = bundle.getString(FILE_LOCATION_BUNDLE_KEY);
        builder.setMessage(String.format(
                        getActivity().getResources().getString(R.string.info_file_written),
                        fileLocation))
                .setPositiveButton(
                        R.string.button_ok,
                        (dialog, id) -> {});
        return builder.create();
    }
}

package com.github.thomasfox.saildata.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.github.thomasfox.saildata.R;

/**
 * Shows the user that a permission is missing
 */
public class PermissionNeededDialogFragment extends DialogFragment {

    private final String permissionName;

    public PermissionNeededDialogFragment(String permissionName) {
        this.permissionName = permissionName;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(String.format(
                        getActivity().getResources().getString(R.string.info_permission_needed),
                        permissionName))
                .setPositiveButton(
                        R.string.button_ok,
                        (dialog, id) -> {}
                        );
        return builder.create();
    }
}

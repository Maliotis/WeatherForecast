package com.maliotis.petros.weather;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;


public class NetworkAlertDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstance){
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.error_title));
        builder.setMessage("Network is unavailable!");
        builder.setPositiveButton(context.getString(R.string.error_ok_button),null);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}

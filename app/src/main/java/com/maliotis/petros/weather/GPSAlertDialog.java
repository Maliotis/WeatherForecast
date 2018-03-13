package com.maliotis.petros.weather;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;


public class GPSAlertDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle Instance){
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.error_title));
        builder.setMessage(context.getString(R.string.error_gps));
        builder.setPositiveButton(context.getString(R.string.error_ok_button),null);
        AlertDialog dialog = builder.create();
        return dialog;
    }

}

package com.maliotis.petros.weather;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;


public class InformUserToGetData extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle instance){
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.error_title))
                .setMessage("No data to display try swiping down to fetch forecast information!")
                .setPositiveButton(context.getString(R.string.error_ok_button),null);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}

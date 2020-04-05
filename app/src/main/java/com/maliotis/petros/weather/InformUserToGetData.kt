package com.maliotis.petros.weather

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class InformUserToGetData : DialogFragment() {
    override fun onCreateDialog(instance: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context?.getString(R.string.error_title))
                .setMessage("No data to display try swiping down to fetch forecast information!")
                .setPositiveButton(context?.getString(R.string.error_ok_button), null)
        return builder.create()
    }
}
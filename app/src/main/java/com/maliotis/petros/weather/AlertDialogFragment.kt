package com.maliotis.petros.weather

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class AlertDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context?.getString(R.string.error_title))
                .setMessage(context?.getString(R.string.error_message))
                .setPositiveButton(context?.getString(R.string.error_ok_button), null)
        return builder.create()
    }
}
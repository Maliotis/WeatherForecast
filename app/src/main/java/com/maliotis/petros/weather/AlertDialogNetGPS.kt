package com.maliotis.petros.weather

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class AlertDialogNetGPS : DialogFragment() {
    override fun onCreateDialog(instance: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context?.getString(R.string.error_title))
        builder.setMessage(context?.getString(R.string.error_net_gps))
        builder.setPositiveButton(context?.getString(R.string.error_ok_button), null)
        return builder.create()
    }
}
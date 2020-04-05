package com.maliotis.petros.weather

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class NetworkAlertDialog : DialogFragment() {
    override fun onCreateDialog(savedInstance: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context?.getString(R.string.error_title))
        builder.setMessage(context?.getString(R.string.error_net))
        builder.setPositiveButton(context?.getString(R.string.error_ok_button), null)
        return builder.create()
    }
}
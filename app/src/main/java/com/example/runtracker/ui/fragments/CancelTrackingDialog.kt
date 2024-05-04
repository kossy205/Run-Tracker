package com.example.runtracker.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog: DialogFragment() {


    private var yesListener: (() -> Unit)? = null

    fun setYesListener(listener: () -> Unit){
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog_AppCompat)
            .setTitle("Cancel Run?")
            .setMessage("Are you sure you want to cancel current run and delete its data?")
            .setIcon(com.example.runtracker.R.drawable.ic_delete)
            .setPositiveButton("Yes"){_, _ ->
                yesListener?.let {yes ->
                    yes()
                }
            }
            .setNegativeButton("No"){dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
    }
}
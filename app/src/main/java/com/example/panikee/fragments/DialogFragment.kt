package com.example.panikee.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import android.app.Dialog
import androidx.fragment.app.DialogFragment
import java.util.*
import android.os.Bundle
import com.example.panikee.R
import com.mapbox.mapboxsdk.style.expressions.Expression.array

class DialogFragment : DialogFragment() {

    internal lateinit var listener: OptionDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?) : Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setTitle("Pilih layanan darurat")
                .setItems(R.array.kejahatan_arrays, DialogInterface.OnClickListener { dialog, id ->
                    Toast.makeText(context, resources.getStringArray(R.array.kejahatan_arrays)[id], Toast.LENGTH_SHORT).show()
                })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as OptionDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + "must implement the dialog listener"))
        }
    }

    interface OptionDialogListener {
        fun onDialogItemsClicked()
    }
}
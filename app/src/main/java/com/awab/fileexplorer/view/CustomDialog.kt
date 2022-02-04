package com.awab.fileexplorer.view

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import com.awab.fileexplorer.R

class CustomDialog{
    companion object {
        fun makeDialog(context: Context, view: View): AlertDialog {
            val dialog = AlertDialog.Builder(context)
                .setView(view)
                .create()

            val d = AppCompatResources.getDrawable(context, R.drawable.dialog_background)
            dialog.window?.setBackgroundDrawable(d)
            dialog.window?.setDimAmount(0.4F)

            return dialog
        }
    }
}
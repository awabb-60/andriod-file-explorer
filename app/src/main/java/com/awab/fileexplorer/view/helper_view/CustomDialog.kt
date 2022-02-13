package com.awab.fileexplorer.view.helper_view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.LoadingLayoutBinding

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

        fun makeLoadingDialog(context: Context): AlertDialog {
            val view = LoadingLayoutBinding.inflate(LayoutInflater.from(context)).root
            val dialog = AlertDialog.Builder(context).setView(view).create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return dialog
        }
    }
}
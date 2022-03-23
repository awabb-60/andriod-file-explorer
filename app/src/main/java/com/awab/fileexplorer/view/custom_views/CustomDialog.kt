package com.awab.fileexplorer.view.custom_views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.LoadingLayoutBinding

class CustomDialog{
    companion object {
        fun makeDialog(context: Context, view: View): AlertDialog {
            val dialog = AlertDialog.Builder(context)
                .setView(view)
                .create()

            // select the right background drawable based on the dark mode state
            val drawable = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO)
                AppCompatResources.getDrawable(context, R.drawable.dialog_background)
            else
                AppCompatResources.getDrawable(context, R.drawable.dialog_background_night)

            dialog.window?.setBackgroundDrawable(drawable)
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
package com.awab.fileexplorer.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.awab.fileexplorer.R

@SuppressLint("ViewConstructor")
class DialogButton(text:String, context:Context, attr:AttributeSet): AppCompatTextView(context, attr) {

    init {
        setText(text)
        alpha = 0.8F
        textSize = 15F
        setPadding(50,10,10,10)
        setTextColor(ContextCompat.getColor(context,R.color.colorOnPrimary))
    }
}
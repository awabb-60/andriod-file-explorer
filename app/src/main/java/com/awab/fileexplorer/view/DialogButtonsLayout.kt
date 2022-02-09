package com.awab.fileexplorer.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class DialogButtonsLayout(context: Context,val attr:AttributeSet): LinearLayout(context, attr) {

    fun addButton(text:String, onClick:()->Unit){

        val button = DialogButton(text, context, attr)
        button.setOnClickListener {
            onClick.invoke()
        }
        addView(button,0)
    }


}
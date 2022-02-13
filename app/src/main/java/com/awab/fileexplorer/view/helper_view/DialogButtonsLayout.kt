package com.awab.fileexplorer.view.helper_view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout

class DialogButtonsLayout(context: Context,val attr:AttributeSet): LinearLayout(context, attr) {

    init {
        // default properties of the button layout
        gravity = Gravity.END or Gravity.CENTER_VERTICAL
    }

    /**
     * add a button the the layout
     * if there is already a button added, the new button will be to the left of the old one
     * @param text the button display text
     * @param onClick the action that will happen after clicking the button
     */
    fun addButton(text:String, onClick:()->Unit){
        val button = DialogButton(text, context, attr)
        button.setOnClickListener {
            onClick.invoke()
        }
        addView(button,0)
    }
}
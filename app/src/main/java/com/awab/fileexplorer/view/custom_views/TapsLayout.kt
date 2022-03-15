package com.awab.fileexplorer.view.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.widget.TextViewCompat
import com.awab.fileexplorer.R

class TapsLayout(context: Context, val attr: AttributeSet) : LinearLayout(context, attr) {

    private val TAG = "TapsLayout"

    private var attributes = context.obtainStyledAttributes(attr, R.styleable.TapsLayout)
    private var firstTapSelected = false


    init {
        orientation = HORIZONTAL
        if (attributes.getBoolean(R.styleable.TapsLayout_show_preview, false) && isInEditMode) {
            addTap(Tap("Tap 1", 0) {})
            addTap(Tap("Tap 2", 4) {})
        }
    }

    fun addTap(tap: Tap) {
        // saving the view index
        tap.viewIndex = childCount

        // making the tap view
        val tapView = TextView(context).apply {
            // setting the title and the appearance
            text = tap.title
            TextViewCompat.setTextAppearance(this, R.style.TextAppearance_AppCompat_Title)

            // adding the given tap height, or wrap_content
            val tapHeight = attributes.getDimension(R.styleable.TapsLayout_tap_height, -1F)
            val newLP = LayoutParams(0, if (tapHeight > -1F) tapHeight.toInt() else LayoutParams.WRAP_CONTENT)

            // setting the taps margins
            attributes.getDimension(R.styleable.TapsLayout_tap_margin, -1F).toInt().also {
                if (it > -1) {
                    newLP.setMargins(it, it, it, it)
                }
            }
            newLP.weight = 1F
            layoutParams = newLP

            gravity = Gravity.CENTER
            // when the tap is click, it will get selected and the function of the tap will get activated
            setOnClickListener {
                tap.function()
                selectTap(tap)
            }
        }

        // adding the tap view to the taps layout
        addView(tapView)
        weightSum = childCount.toFloat()

        // select tha first tap auto
        if (!firstTapSelected)
            firstTapSelected = tapView.callOnClick()
    }

    private fun selectTap(tap: Tap) {
        val viewIterator = children.iterator()
        // removing the background from all taps
        while (viewIterator.hasNext())
            viewIterator.next().background = null

        // selecting the targeted tap
        getChildAt(tap.viewIndex).background =
            attributes.getDrawable(R.styleable.TapsLayout_tap_background_drawable)
    }
}

data class Tap(val title: String, var viewIndex: Int = 0, val function: () -> Unit)
package com.awab.fileexplorer.view.custom_views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.widget.TextViewCompat
import com.awab.fileexplorer.R
import com.awab.fileexplorer.utils.data.data_models.Tap

class TapsLayout(context: Context, val attr: AttributeSet) : LinearLayout(context, attr) {
    private val TAG = "TapsLayout"

    private var attributes = context.obtainStyledAttributes(attr, R.styleable.TapsLayout)
    private val taps = mutableListOf<View>()
    private lateinit var selectedTap: View

    init {
        orientation = HORIZONTAL

        // to show a preview of the layout while the editor is active
        if (attributes.getBoolean(R.styleable.TapsLayout_show_preview, false) && isInEditMode) {
            addTap(Tap("Tap 1", 0) {})
            addTap(Tap("Tap 2", 1) {})
            selectTap(0)
        }
    }

    fun addTap(tap: Tap) {
        // saving the view index
        tap.viewIndex = childCount

        // making the tap view
        val tapView = TextView(context).apply {
            taps.add(this)
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

//        // select tha first tap auto
//        if (!firstTapSelected)
//            firstTapSelected = tapView.callOnClick()
    }

    fun selectTap(index: Int) {
        if (index in taps.indices)
            taps[index].callOnClick()
    }

    private fun selectTap(tap: Tap) {
        // removing the background from all taps
        children.forEach {
            it.background = null
            it.alpha = 0.5F
        }

        // selecting the targeted tap
        selectedTap = getChildAt(tap.viewIndex)
        selectedTap.background =
            attributes.getDrawable(R.styleable.TapsLayout_tap_background_drawable)
        selectedTap.alpha = 1F
    }

    /**
     * it recall the saved function of the selected tap
     * make it look like the tap was clicked again
     */
    fun refreshCurrentTap() {
        try {
            selectedTap.callOnClick()
        } catch (e: Exception) {
            Log.d(TAG, "cant refresh the current tap")
        }
    }

}
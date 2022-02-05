package com.awab.fileexplorer.adapters

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation


class Anim(val width: Int, val view: View) : Animation() {
    private val startWidth = view.width

    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        val newWidth = startWidth + ((width - startWidth) * interpolatedTime).toInt()
        view.layoutParams.width = newWidth
        view.requestLayout()
        super.applyTransformation(interpolatedTime, t)
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}
package com.orgzly.android.ui.notes

import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.orgzly.R
import com.orgzly.android.ui.util.styledAttributes

object SelectionBackground {
    fun setIfSelected(isSelected: Boolean, view: View, alpha: Int? = null) {
        if (isSelected) {
            val color = backgroundColor(view, alpha)
            view.setBackgroundColor(color)
        } else {
            view.setBackgroundResource(0)
        }
    }

    @ColorInt
    private fun backgroundColor(view: View, alpha: Int? = null): Int  {
        var color = view.context.styledAttributes(intArrayOf(com.google.android.material.R.attr.colorPrimaryInverse)) { typedArray ->
            typedArray.getColor(0, 0)
        }

        if (alpha != null) {
            color = ColorUtils.setAlphaComponent(color, alpha)
        }

        return color
    }
}
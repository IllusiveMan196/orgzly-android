package com.orgzly.android.ui.dndrv

import androidx.recyclerview.widget.RecyclerView

fun interface OnStartDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}
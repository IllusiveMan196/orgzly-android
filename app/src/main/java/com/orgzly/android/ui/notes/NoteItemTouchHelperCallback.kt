package com.orgzly.android.ui.notes

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.orgzly.BuildConfig
import com.orgzly.android.ui.SelectableItemAdapter
import com.orgzly.android.util.LogUtils
import kotlin.math.abs
import kotlin.math.min

class NoteItemTouchHelperCallback(val adapter: SelectableItemAdapter, val listener: Listener) : ItemTouchHelper.Callback() {
    fun interface Listener {
        fun onSwipe(viewHolder: RecyclerView.ViewHolder, direction: Int)
    }

    override fun getAnimationDuration(
        recyclerView: RecyclerView,
        animationType: Int,
        animateDx: Float,
        animateDy: Float
    ): Long {
        // return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
        return 50
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        // return super.getSwipeThreshold(viewHolder)
        return 0.1f
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, recyclerView, viewHolder)

        return makeMovementFlags(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, viewHolder, direction)
        listener.onSwipe(viewHolder, direction)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, viewHolder)

        SelectionBackground.setIfSelected(
            adapter.getSelection().contains(viewHolder.itemId), viewHolder.itemView)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (BuildConfig.LOG_DEBUG)
            LogUtils.d(TAG, actionState, dX, viewHolder.itemView.width.toFloat(), dY)

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val maxWidth = viewHolder.itemView.width.toFloat()

            val alpha1 = 1f - min(abs(dX), maxWidth) / maxWidth
            val alpha255 = alpha1 * 255
            val alpha = (255 - alpha255).toInt()

            // If item is selected, don't touch the background
            if (!adapter.getSelection().contains(viewHolder.itemId)) {
                SelectionBackground.setIfSelected(true, viewHolder.itemView, alpha)
            }
        }

        // super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    companion object {
        private val TAG = NoteItemTouchHelperCallback::class.java.name
    }
}
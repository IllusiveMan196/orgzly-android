package com.orgzly.android.ui.notes

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.orgzly.BuildConfig
import com.orgzly.R
import com.orgzly.android.prefs.NotePopupPreference
import com.orgzly.android.ui.util.getLayoutInflater
import com.orgzly.android.util.LogUtils


fun interface NotePopupListener {
    fun onPopupButtonClick(itemId: Long, @IdRes buttonId: Int)
}


object NotePopup {
    enum class Location {
        BOOK,
        QUERY
    }

    fun showWindow(itemId: Long, anchor: View, location: Location, direction: Int, listener: NotePopupListener): PopupWindow? {
        val context = anchor.context

        val actions = getActionsForLocation(context, location, direction)

        // If there is only one button just perform the action
        if (actions.size == 1) {
            listener.onPopupButtonClick(itemId, actions.first().id)
            return null

        } else if (actions.isEmpty()) {
            // TODO: Don't allow in preference, and/or show a snackbar here
            return null
        }

        val popupView = context.getLayoutInflater().inflate(R.layout.note_popup_buttons, null)

        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT

        val popupWindow = PopupWindow(popupView, width, height, false).apply {
            isOutsideTouchable = true

            // Required on API 21 and 22 (Lollipop) so it can be dismissed on outside click
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val group = popupView.findViewById<ViewGroup>(R.id.group)

        val inflater = context.getLayoutInflater()

        actions.forEach { action ->
            val button = inflater.inflate(R.layout.note_popup_button, null) as MaterialButton

            button.setOnClickListener {
                listener.onPopupButtonClick(itemId, action.id)
                popupWindow.dismiss()
            }

            button.setIconResource(action.icon)

            group.addView(button)
        }

        val gravity = Gravity.START or Gravity.TOP

        // Top left of the anchor
        val (anchorX, anchorY) = IntArray(2).also { arr ->
            anchor.getLocationInWindow(arr)
        }

        // Avoid resizing popup if it cannot fit.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            popupWindow.setIsClippedToScreen(true)
//        }

        // Don't use showAsDropDown() if anchor's top is out of view (to avoid scrolling).
        // popupWindow.setAllowScrollingAnchorParent(false)
        if (anchorY < 0) {
            val yOffset =
                anchor.rootView.findViewById<MaterialToolbar>(R.id.top_toolbar).let { toolbar ->
                    toolbar?.layoutParams?.height ?: anchorY
                }

            if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, anchorY, "showAtLocation", anchorX, yOffset)
            popupWindow.showAtLocation(anchor, gravity, anchorX, yOffset)

        } else {
            if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, anchorY, "showAsDropDown")
            popupWindow.showAsDropDown(anchor)
        }

        return popupWindow
    }

    private fun getActionsForLocation(context: Context, location: Location, direction: Int): List<Action> {
        val keyId = preferenceKeyForLocation(location, direction)
        val key = context.getString(keyId)

        return NotePopupPreference.getSelected(context, key)
    }

    private fun preferenceKeyForLocation(location: Location, direction: Int): Int {
        return when {
            location == Location.BOOK && direction == ItemTouchHelper.LEFT ->
                R.string.pref_key_note_popup_buttons_in_book_left
            location == Location.BOOK && direction == ItemTouchHelper.RIGHT ->
                R.string.pref_key_note_popup_buttons_in_book_right
            location == Location.QUERY && direction == ItemTouchHelper.LEFT ->
                R.string.pref_key_note_popup_buttons_in_query_left
            location == Location.QUERY && direction == ItemTouchHelper.RIGHT ->
                R.string.pref_key_note_popup_buttons_in_query_right

            else -> throw IllegalArgumentException("No buttons for $location/$direction")
        }
    }

    data class Action(@IdRes val id : Int, @DrawableRes val icon: Int, val name: String)

    val allActions = listOf(
        Action(R.id.note_popup_set_schedule, R.drawable.ic_today, "set-schedule"),
        Action(R.id.note_popup_set_deadline, R.drawable.ic_alarm, "set-deadline"),
        Action(R.id.note_popup_set_state, R.drawable.ic_flag, "set-state"),
        Action(R.id.note_popup_toggle_state, R.drawable.ic_check_circle_outline, "toggle-state"),
        Action(R.id.note_popup_clock_in, R.drawable.ic_hourglass_top, "clock-in"),
        Action(R.id.note_popup_clock_out, R.drawable.ic_hourglass_bottom, "clock-out"),
        Action(R.id.note_popup_clock_cancel, R.drawable.ic_hourglass_disabled, "clock-cancel"),
        Action(R.id.note_popup_delete, R.drawable.ic_delete, "delete"),
        Action(R.id.note_popup_new_above, R.drawable.cic_new_above, "new-above"),
        Action(R.id.note_popup_new_under, R.drawable.cic_new_under, "new-under"),
        Action(R.id.note_popup_new_below, R.drawable.cic_new_below, "new-below"),
        Action(R.id.note_popup_refile, R.drawable.ic_move_to_inbox, "refile"),
        Action(R.id.note_popup_focus, R.drawable.ic_center_focus_strong, "focus"),
    )

    private val TAG = NotePopup::class.java.name
}

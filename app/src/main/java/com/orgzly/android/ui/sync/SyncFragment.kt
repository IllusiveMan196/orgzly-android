package com.orgzly.android.ui.sync

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orgzly.BuildConfig
import com.orgzly.R
import com.orgzly.android.App
import com.orgzly.android.prefs.AppPreferences
import com.orgzly.android.sync.SyncRunner
import com.orgzly.android.sync.SyncState
import com.orgzly.android.sync.SyncState.Companion.getInstance
import com.orgzly.android.ui.util.copyPlainTextToClipboard
import com.orgzly.android.usecase.UseCase
import com.orgzly.android.usecase.UseCaseResult
import com.orgzly.android.usecase.UseCaseRunner
import com.orgzly.android.util.LogUtils.d
import com.orgzly.databinding.FragmentSyncBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Retained fragment for user actions.
 */
@AndroidEntryPoint
class SyncFragment : Fragment() {
    private lateinit var binding: FragmentSyncBinding

    /** Activity which has this fragment attached. Used as a target for hooks.  */
    private var mListener: Listener? = null

    private val viewModel: SyncViewModel by viewModels()

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        /* This makes sure that the container activity has implemented
         * the callback interface. If not, it throws an exception
         */
        mListener = try {
            activity as Listener?
        } catch (e: ClassCastException) {
            throw ClassCastException(requireActivity().toString() + " must implement " + Listener::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (BuildConfig.LOG_DEBUG) d(TAG, savedInstanceState)

        binding = FragmentSyncBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val syncButton = SyncButton(view)

        viewModel.state.observe(viewLifecycleOwner) { state: SyncState ->
            if (state.isRunning()) {
                // Allow snackbar for failures as soon as sync has been seen working
                viewModel.allowSnackbarOnFailure = true
            }

            if (state.type != SyncState.Type.NEVER_RAN) {
                syncButton.updateUi(state)

                if (!state.isRunning()) {
                    if (viewModel.allowSnackbarOnFailure && state.isFailure()) {
                        activity?.let {
                            SyncRunner.showSyncFailedSnackBar(it, state)
                        }
                    }

                    // Disallow snackbar if sync is not running
                    viewModel.allowSnackbarOnFailure = false
                }

            } else {
                // Never ran
                syncButton.updateUi(getInstance(SyncState.Type.FINISHED))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (BuildConfig.LOG_DEBUG) d(TAG)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.LOG_DEBUG) d(TAG)
    }

    /**
     * Set the callback to null so we don't accidentally leak the Activity instance.
     */
    override fun onDetach() {
        super.onDetach()
        if (BuildConfig.LOG_DEBUG) d(TAG)
        mListener = null
    }

    /*
     * Sync button which should be updated from the main UI thread.
     */
    private inner class SyncButton(view: View) {
        private val context: Context
        private val rotation: Animation

        fun updateUi(state: SyncState) {
            setButtonTextToStateOrLastSynced(state)

            setButtonIconAnimation(state)
        }

        private fun setButtonTextToStateOrLastSynced(state: SyncState) {
            getContext()?.let {
                var msg = state.getDescription(context)

                // If state description returns null, use the last sync time
                if (msg == null) {
                    msg = lastSyncTime()
                }

                binding.syncButtonText.text = msg
            }
        }

        private fun lastSyncTime(): String {
            val time = AppPreferences.lastSuccessfulSyncTime(context)

            return if (time > 0) {
                resources.getString(R.string.last_sync_with_argument, formatLastSyncTime(time))
            } else {
                resources.getString(R.string.sync)
            }
        }

        private fun formatLastSyncTime(time: Long): String {
            return DateUtils.formatDateTime(
                context,
                time,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_TIME
            )
        }

        private fun setButtonIconAnimation(state: SyncState) {
            when (state.type) {
                SyncState.Type.CANCELING,
                SyncState.Type.STARTING,
                SyncState.Type.COLLECTING_BOOKS,
                SyncState.Type.BOOKS_COLLECTED -> {
                    setAnimation(true)
                }

                SyncState.Type.BOOK_STARTED,
                SyncState.Type.BOOK_ENDED -> {
                    setAnimation(true)
                }

                SyncState.Type.NEVER_RAN,
                SyncState.Type.AUTO_SYNC_NOT_STARTED,
                SyncState.Type.FINISHED,
                SyncState.Type.CANCELED,
                SyncState.Type.FAILED_NO_REPOS,
                SyncState.Type.FAILED_NO_CONNECTION,
                SyncState.Type.FAILED_NO_STORAGE_PERMISSION,
                SyncState.Type.FAILED_NO_BOOKS_FOUND,
                SyncState.Type.FAILED_EXCEPTION -> {
                    setAnimation(false)
                }
            }
        }

        private fun setAnimation(shouldAnimate: Boolean) {
            if (shouldAnimate) {
                if (binding.syncButtonIcon.animation == null) {
                    binding.syncButtonIcon.startAnimation(rotation)
                }
            } else {
                if (binding.syncButtonIcon.animation != null) {
                    binding.syncButtonIcon.clearAnimation()
                }
            }
        }

        init {
            context = view.context

            rotation = AnimationUtils.loadAnimation(context, R.anim.rotate_counterclockwise)
            rotation.repeatCount = Animation.INFINITE

            binding.syncButtonContainer.run {
                setOnClickListener {
                    if (viewModel.isSyncRunning()) {
                        SyncRunner.stopSync(context)
                    } else {
                        SyncRunner.startSync(context)
                    }
                }

                setOnLongClickListener {
                    val dialog: Dialog = MaterialAlertDialogBuilder(requireContext())
                        .setPositiveButton(R.string.ok, null)
                        .setNeutralButton(R.string.copy) { _: DialogInterface?, _: Int ->
                            context.copyPlainTextToClipboard("Sync output", binding.syncButtonText.text)
                        }
                        .setMessage(binding.syncButtonText.text)
                        .show()
                    setDialogMessageSelectable(dialog)
                    true
                }
            }
        }
    }

    private fun setDialogMessageSelectable(dialog: Dialog) {
        dialog.window?.let { window ->
            val textView = window.decorView.findViewById<TextView>(android.R.id.message)
            textView?.setTextIsSelectable(true)
        }
    }

    fun run(action: UseCase) {
        App.EXECUTORS.diskIO().execute {
            try {
                val result = UseCaseRunner.run(action)
                App.EXECUTORS.mainThread().execute {
                    mListener?.onSuccess(action, result)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                App.EXECUTORS.mainThread().execute {
                    mListener?.onError(action, e)
                }
            }
        }
    }

    interface Listener {
        fun onSuccess(action: UseCase?, result: UseCaseResult?)
        fun onError(action: UseCase?, throwable: Throwable?)
    }

    companion object {
        private val TAG = SyncFragment::class.java.name

        @JvmField
        val FRAGMENT_TAG: String = SyncFragment::class.java.name

        @JvmStatic
        val instance: SyncFragment
            get() = SyncFragment()
    }
}
package com.orgzly.android.sync

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.work.*
import com.orgzly.BuildConfig
import com.orgzly.R
import com.orgzly.android.ui.repos.ReposActivity
import com.orgzly.android.ui.showSnackbar
import com.orgzly.android.util.LogUtils
import com.orgzly.android.workers.DelegatingCoroutineWorker
import com.orgzly.android.workers.delegatedData

object SyncRunner {
    const val IS_AUTO_SYNC = "auto-sync"

    private val TAG: String = SyncRunner::class.java.name

    private const val UNIQUE_WORK_NAME = "sync"

    @JvmStatic
    fun startAuto(context: Context) {
        startSync(context, true)
    }

    @JvmStatic
    @JvmOverloads
    fun startSync(context: Context, autoSync: Boolean = false) {
        val workManager = WorkManager.getInstance(context)

        val syncWorker = OneTimeWorkRequestBuilder<DelegatingCoroutineWorker>()
//            // On Android >= 12 notification from overridden getForegroundInfo might not be shown
//            // Sync-in-progress notification cannot be canceled if app is killed by the system,
//            // when handling notification manually from the worker.
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(workDataOf(
                SyncWorker::class.delegatedData(),
                IS_AUTO_SYNC to autoSync))
            .build()

        workManager
            .beginUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, syncWorker)
            .enqueue()
    }

    @JvmStatic
    fun showSyncFailedSnackBar(activity: FragmentActivity, state: SyncState) {
        if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, activity, state)

        val msg = state.getDescription(activity)

        activity.showSnackbar(msg, R.string.repositories) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setClass(activity, ReposActivity::class.java)
            }
            startActivity(activity, intent, null)
        }
    }

    @JvmStatic
    fun stopSync(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    @JvmStatic
    fun onStateChange(context: Context, tag: String): LiveData<SyncState> {
        return onAllWorkInfo(context).map { workInfoList ->
            syncStateFromWorkInfoList(workInfoList).also { state ->
                logStateChange(tag, state, workInfoList)
            }
        }

//        return MediatorLiveData<SyncState>().apply {
//            addSource(onInitWorkInfo()) {
//                value = state
//            }
//
//            addSource(onMainWorkInfo()) {
//                value = state
//            }
//        }
    }

    private fun logStateChange(tag: String, state: SyncState?, workInfoList: List<WorkInfo>?) {
        if (BuildConfig.LOG_DEBUG) {
            // LogUtils.d(TAG, "-> ($tag) Workers changed state to $state <- $workInfoList")
            LogUtils.d(TAG, "-> ($tag) Workers changed state to $state <- ${workInfoList?.map { it.state }}")
        }
    }

    private fun onAllWorkInfo(context: Context): LiveData<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME)
    }

    private fun syncStateFromWorkInfoList(workInfoList: List<WorkInfo>): SyncState {
        if (workInfoList.isNotEmpty()) {
            val oneAndOnlyWorker = workInfoList.first()

            oneAndOnlyWorker.run {
                if (state == WorkInfo.State.CANCELLED) {
                    return SyncState.getInstance(SyncState.Type.CANCELED)

                } else if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                    return SyncState.fromData(progress)

                } else if (state.isFinished) {
                    return SyncState.fromData(outputData)
                }
            }
        }

        return SyncState.getInstance(SyncState.Type.FINISHED)
    }
}
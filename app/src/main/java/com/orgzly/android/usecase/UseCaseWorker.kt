package com.orgzly.android.usecase

import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import com.orgzly.BuildConfig
import com.orgzly.android.AppIntent
import com.orgzly.android.data.DataRepository
import com.orgzly.android.prefs.AppPreferences
import com.orgzly.android.util.LogUtils
import com.orgzly.android.workers.DelegatingWorker
import com.orgzly.android.workers.delegatedData
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UseCaseWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val params: WorkerParameters,
    val dataRepository: DataRepository
) : Worker(context, params) {

    override fun doWork(): Result {
        if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, params.inputData)

        when (params.inputData.getString(DATA_ACTION)) {
            AppIntent.ACTION_IMPORT_GETTING_STARTED_NOTEBOOK -> {
                postGettingStartedImport {
                    UseCaseRunner.run(BookImportGettingStarted())
                }
            }

            AppIntent.ACTION_REPARSE_NOTES -> {
                broadcastNotesUpdate {
                    UseCaseRunner.run(NoteReparseStateAndTitles())
                }
            }

            AppIntent.ACTION_SYNC_CREATED_AT_WITH_PROPERTY -> {
                broadcastNotesUpdate {
                    UseCaseRunner.run(NoteSyncCreatedAtTimeWithProperty())
                }
            }

            AppIntent.ACTION_UPDATE_TIMESTAMPS -> {
                UseCaseRunner.run(TimestampUpdate())
            }
        }

        return Result.success()
    }

    private fun broadcastNotesUpdate(action: () -> Unit) {
        LocalBroadcastManager.getInstance(context).apply {
            sendBroadcast(Intent(AppIntent.ACTION_UPDATING_NOTES_STARTED))
            action()
            sendBroadcast(Intent(AppIntent.ACTION_UPDATING_NOTES_ENDED))
        }
    }

    private fun postGettingStartedImport(action: () -> Unit) {
        action()

        /* If notebook was already previously loaded, it's a user-requested reload.
         * Display a message in that case.
         */
        if (AppPreferences.isGettingStartedNotebookLoaded(context)) {
            val intent = Intent(AppIntent.ACTION_BOOK_IMPORTED)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

        } else {
            AppPreferences.isGettingStartedNotebookLoaded(context, true)
        }
    }

    companion object {
        val TAG: String = UseCaseWorker::class.java.name

        private const val DATA_ACTION = "action"

        @JvmStatic
        fun schedule(context: Context, useCase: UseCase) {
            val workManager = WorkManager.getInstance(context)

            val request = OneTimeWorkRequestBuilder<DelegatingWorker>()
                .setInputData(workDataOf(
                    UseCaseWorker::class.delegatedData(),
                    DATA_ACTION to useCase.toAction()))
                .build()

            val uniqueWorkName = useCase::class.simpleName!! // TODO: add name to UseCase

            workManager.enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.KEEP, request)
        }
    }
}

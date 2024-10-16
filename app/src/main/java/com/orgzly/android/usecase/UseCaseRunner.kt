package com.orgzly.android.usecase

import com.orgzly.BuildConfig
import com.orgzly.android.App
import com.orgzly.android.SharingShortcutsManager
import com.orgzly.android.data.DataRepository
import com.orgzly.android.reminders.RemindersScheduler
import com.orgzly.android.sync.AutoSync
import com.orgzly.android.util.LogUtils
import com.orgzly.android.widgets.ListWidgetProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent


object UseCaseRunner {
    private val TAG = UseCaseRunner::class.java.name

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UseCaseRunnerEntryPoint {
        val autoSync: AutoSync
        val dataRepository: DataRepository
    }

    // FIXME: Make UseCaseRunner a class
    // class Factory @Inject constructor(val autoSync: AutoSync, val dataRepository: DataRepository)

    @JvmStatic
    fun run(action: UseCase): UseCaseResult {
        val startedAt = System.currentTimeMillis()

        val utilitiesEntryPoint =
            EntryPointAccessors.fromApplication(
                App.appContext, UseCaseRunnerEntryPoint::class.java)

        val result = action.run(utilitiesEntryPoint.dataRepository)

        when (result.triggersSync) {
            UseCase.SYNC_DATA_MODIFIED -> utilitiesEntryPoint.autoSync.trigger(AutoSync.Type.DATA_MODIFIED)
            UseCase.SYNC_NOTE_CREATED -> utilitiesEntryPoint.autoSync.trigger(AutoSync.Type.NOTE_CREATED)
        }

        if (result.modifiesLocalData) {
            RemindersScheduler.notifyDataSetChanged(App.appContext)
            ListWidgetProvider.notifyDataSetChanged(App.appContext)
            SharingShortcutsManager(utilitiesEntryPoint.dataRepository).replaceDynamicShortcuts(App.appContext)
        }

        if (result.modifiesListWidget) {
            ListWidgetProvider.update(App.appContext)
        }

        if (BuildConfig.LOG_DEBUG) {
            val ms = System.currentTimeMillis() - startedAt
            LogUtils.d(TAG, "Finished $action in ${ms}ms")
        }

        return result
    }
}
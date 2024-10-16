package com.orgzly.android.sync

import android.app.Application
import android.content.Context
import com.orgzly.BuildConfig
import com.orgzly.android.data.DataRepository
import com.orgzly.android.prefs.AppPreferences
import com.orgzly.android.util.LogUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoSync @Inject constructor(
    @ApplicationContext val context: Context,
    val dataRepository: DataRepository
) {

    fun trigger(type: Type) {
        if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, type)

        if (AppPreferences.autoSync(context)) {
            when (type) {
                Type.NOTE_CREATED ->
                    if (AppPreferences.syncOnNoteCreate(context)) {
                        startSync()
                    }

                Type.DATA_MODIFIED ->
                    if (AppPreferences.syncOnNoteUpdate(context)) {
                        startSync()
                    }

                Type.APP_RESUMED ->
                    if (AppPreferences.syncOnResume(context)) {
                        startSync()
                    }
            }
        }
    }

    private fun startSync() {
        if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG)

        SyncRunner.startAuto(context)
    }

    enum class Type {
        NOTE_CREATED,
        DATA_MODIFIED,
        APP_RESUMED
    }

    companion object {
        private val TAG = AutoSync::class.java.name
    }
}
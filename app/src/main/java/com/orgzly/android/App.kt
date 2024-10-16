package com.orgzly.android

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import androidx.preference.PreferenceManager
import com.orgzly.android.NotificationChannels.createAll
import com.orgzly.android.ui.settings.SettingsFragment
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
open class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        appContext = applicationContext

        super.onCreate()

//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//            .detectAll()
//            .penaltyLog()
//            .build());

        setDefaultPreferences(this, false)

        createAll(this)
    }

    companion object {
        // TODO: Get rid of this, use coroutines
        @JvmField
        var EXECUTORS: AppExecutors = AppExecutors()

        // TODO: Get rid of this
        lateinit var appContext: Context

        @JvmStatic
        fun setDefaultPreferences(context: Context, readAgain: Boolean) {
            if (readAgain || !PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)
            ) {
                for (res in SettingsFragment.PREFS_RESOURCES.values) {
                    PreferenceManager.setDefaultValues(context, res, true)
                }
            }
        }
    }
}

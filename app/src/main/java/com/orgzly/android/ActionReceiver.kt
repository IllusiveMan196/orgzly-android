package com.orgzly.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.orgzly.android.AppIntent.ACTION_SYNC_START
import com.orgzly.android.AppIntent.ACTION_SYNC_STOP
import com.orgzly.android.sync.SyncRunner
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, receivedIntent: Intent) {
        when (receivedIntent.action) {
            ACTION_SYNC_START -> {
                SyncRunner.startSync(context);
            }

            ACTION_SYNC_STOP -> {
                SyncRunner.stopSync(context);
            }
        }
    }
}
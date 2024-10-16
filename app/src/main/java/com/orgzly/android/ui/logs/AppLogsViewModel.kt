package com.orgzly.android.ui.logs

import com.orgzly.android.data.logs.AppLogsRepository
import com.orgzly.android.ui.CommonViewModel
import com.orgzly.android.util.LogMajorEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AppLogsViewModel @Inject constructor(appLogsRepository: AppLogsRepository) : CommonViewModel() {
    val logs = appLogsRepository.getFlow(LogMajorEvents.REMINDERS).map {
        it.map { logEntry ->
            val date = Date(logEntry.time)
            val type = logEntry.type
            val message = logEntry.message

            "$date $type $message"
        }
    }
}
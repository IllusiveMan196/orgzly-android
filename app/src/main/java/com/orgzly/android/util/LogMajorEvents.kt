package com.orgzly.android.util

import android.content.Context
import com.orgzly.android.prefs.AppPreferences

class LogMajorEvents {
    companion object {

        const val REMINDERS = "reminders"

        fun isEnabled(context: Context): Boolean {
            return AppPreferences.logMajorEvents(context)
        }
    }

//    class VM : ViewModel() {
//        fun logCatOutput(name: String) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
//            // Runtime.getRuntime().exec("logcat -c")
//            // val tag = tagWithName(name)
//            // Runtime.getRuntime().exec("logcat *:S $tag")
//            Runtime.getRuntime().exec("logcat")
//                .inputStream
//                .bufferedReader()
//                .useLines { lines ->
//                    lines.forEach { line ->
//                        if (line.contains("orgzly")) {
//                            emit(line)
//                        }
//                    }
//                }
//        }
//    }

//    fun readLogCatOutput(name: String): List<String> {
//        val output = mutableListOf<String>()
//        Runtime.getRuntime().exec("logcat -d")
//            .inputStream
//            .bufferedReader()
//            .useLines { lines ->
//                lines.forEach { line ->
//                    if (line.contains("orgzly")) {
//                        output.add(line)
//                    }
//                }
//            }
//        return output
//    }
}

package dev.marcelsoftware.mpvcompose

import android.util.Log

class DefaultLogObserver : MPVLib.LogObserver {
    override fun logMessage(
        prefix: String,
        level: Int,
        text: String,
    ) {
        when (level) {
            MPVLib.LogLevel.MPV_LOG_LEVEL_FATAL -> Log.wtf(prefix, text)
            MPVLib.LogLevel.MPV_LOG_LEVEL_ERROR -> Log.e(prefix, text)
            MPVLib.LogLevel.MPV_LOG_LEVEL_WARN -> Log.w(prefix, text)
            MPVLib.LogLevel.MPV_LOG_LEVEL_INFO -> Log.i(prefix, text)
            MPVLib.LogLevel.MPV_LOG_LEVEL_V -> Log.v(prefix, text)
            MPVLib.LogLevel.MPV_LOG_LEVEL_DEBUG -> Log.d(prefix, text)
            MPVLib.LogLevel.MPV_LOG_LEVEL_TRACE -> Log.v(prefix, text)
            else -> Log.d(prefix, text)
        }
    }
}

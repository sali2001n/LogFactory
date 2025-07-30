package com.sali.logfactory.logger

import android.content.Context
import android.util.Log
import com.sali.logfactory.models.LogEntry
import com.sali.logfactory.models.LogType

class LogcatLogger : ILogger {

    override fun initialize(context: Context) {
        // No need for specific initialization
    }

    override fun log(logEntry: LogEntry) {
        logEntry.apply {
            when (logType) {
                LogType.Debug -> Log.d(tag, message, throwable)
                LogType.Error -> Log.e(tag, message, throwable)
                LogType.Info -> Log.i(tag, message, throwable)
                LogType.Verbose -> Log.v(tag, message, throwable)
                LogType.Warn -> Log.w(tag, message, throwable)
            }
        }
    }
}
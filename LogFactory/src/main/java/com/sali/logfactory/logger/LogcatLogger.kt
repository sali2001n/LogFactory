package com.sali.logfactory.logger

import android.content.Context
import android.util.Log
import com.sali.logfactory.formatter.LogMessageFormatter
import com.sali.logfactory.models.LogEntry
import com.sali.logfactory.models.LogType

class LogcatLogger(val formatter: LogMessageFormatter? = null) : ILogger {

    override fun initialize(context: Context) {
        // No need for specific initialization
    }

    override fun log(logEntry: LogEntry) {
        if (formatter != null) {
            println(formatter.format(logEntry))
        } else {
            when (logEntry.logType) {
                LogType.Debug -> Log.d(logEntry.tag, logEntry.message, logEntry.throwable)
                LogType.Error -> Log.e(logEntry.tag, logEntry.message, logEntry.throwable)
                LogType.Info -> Log.i(logEntry.tag, logEntry.message, logEntry.throwable)
                LogType.Verbose -> Log.v(logEntry.tag, logEntry.message, logEntry.throwable)
                LogType.Warn -> Log.w(logEntry.tag, logEntry.message, logEntry.throwable)
            }
        }
    }
}
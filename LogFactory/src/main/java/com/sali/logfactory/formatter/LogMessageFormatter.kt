package com.sali.logfactory.formatter

import android.util.Log
import com.sali.logfactory.models.LogEntry
import java.text.SimpleDateFormat
import java.util.Locale

interface LogMessageFormatter {
    fun format(logEntry: LogEntry): String
}

object DefaultLogMessageFormatter : LogMessageFormatter {
    override fun format(logEntry: LogEntry): String {
        val timestamp = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS",
            Locale.getDefault()
        ).format(logEntry.date)
        return if (logEntry.throwable != null) {
            "$timestamp [${logEntry.logType.name}] [${logEntry.tag}]: ${logEntry.message}\n${
                Log.getStackTraceString(
                    logEntry.throwable
                )
            }"
        } else {
            "$timestamp [${logEntry.logType.name}] [${logEntry.tag}]: ${logEntry.message}"
        }
    }

}
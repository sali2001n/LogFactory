package com.sali.logfactory.logger

import android.util.Log
import com.sali.logfactory.formatter.LogMessageFormatter
import com.sali.logfactory.models.LogEntry
import com.sali.logfactory.models.LogType

/**
 * [LogcatLogger] is an implementation of [ILogger] that logs messages to Android's Logcat.
 *
 * This logger supports two modes:
 *
 * 1. **Default Logcat Logging:**
 *    If no custom formatter is provided, logs are routed through Android's `Log.d`, `Log.i`, etc.,
 *    preserving the log level colors and system timestamp formatting.
 *
 * 2. **Custom Log Formatting (full control):**
 *    If a [LogMessageFormatter] is provided, the logger bypasses Android's Logcat API
 *    and uses `println()` to output logs. This gives full control over the format and layout
 *    but disables automatic coloring and tagging by Logcat.
 *
 * @property formatter Optional [LogMessageFormatter] to customize the log format.
 */
class LogcatLogger(val formatter: LogMessageFormatter? = null) : ILogger {

    /**
     * Logs a message to Logcat or prints a custom-formatted message using [formatter].
     *
     * - If [formatter] is `null`, uses Android's `Log` class to log with the appropriate severity.
     * - If [formatter] is set, calls `println()` with the formatted string.
     *
     * @param logEntry The log data to record, including level, message, tag, and throwable.
     */
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
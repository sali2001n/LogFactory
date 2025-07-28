package com.sali.logfactory.logger

import android.content.Context
import com.sali.logfactory.models.LogEntry

interface ILogger {

    /**
     * Initializes the logger by preparing the target file location.
     * This method should be called before any logging attempt.
     */
    fun initialize(context: Context)


    /**
     * Logs the given entry by formatting it and writing it.
     */
    fun log(logEntry: LogEntry)

}
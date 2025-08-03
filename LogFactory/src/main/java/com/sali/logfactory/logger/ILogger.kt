package com.sali.logfactory.logger

import com.sali.logfactory.models.LogEntry

interface ILogger {

    /**
     * Logs the given entry by formatting it and writing it.
     */
    fun log(logEntry: LogEntry)

}
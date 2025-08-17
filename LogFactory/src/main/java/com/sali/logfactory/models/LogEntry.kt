package com.sali.logfactory.models

import java.util.Date

enum class LogType {
    DEBUG, ERROR, INFO, VERBOSE, WARN,
}

data class LogEntry(
    val logType: LogType,
    val date: Date,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null,
)

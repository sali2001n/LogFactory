package com.sali.logfactory.models

import java.util.Date

enum class LogType(val priority: Int) {
    VERBOSE(1),
    DEBUG(2),
    INFO(3),
    WARN(4),
    ERROR(5),
}

data class LogEntry(
    val logType: LogType,
    val date: Date,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null,
)

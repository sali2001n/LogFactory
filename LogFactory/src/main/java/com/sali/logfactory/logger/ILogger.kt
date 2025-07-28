package com.sali.logfactory.logger

import android.content.Context
import com.sali.logfactory.models.LogEntry

interface ILogger {

    fun initialize(context: Context)

    fun log(logEntry: LogEntry)

}
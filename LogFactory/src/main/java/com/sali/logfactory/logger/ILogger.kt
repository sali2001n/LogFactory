package com.sali.logfactory.logger

import android.content.Context
import com.sali.logfactory.models.LogConfig
import com.sali.logfactory.models.LogEntry

interface ILogger {

    fun initialize(context: Context, config: LogConfig)

    fun log(logEntry: LogEntry)

}
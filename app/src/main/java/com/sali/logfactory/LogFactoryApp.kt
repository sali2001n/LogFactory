package com.sali.logfactory

import android.app.Application
import com.sali.logfactory.factory.LogFactory
import com.sali.logfactory.formatter.LogMessageFormatter
import com.sali.logfactory.logger.FileLogger
import com.sali.logfactory.models.LogConfig
import com.sali.logfactory.models.LogEntry

class LogFactoryApp : Application() {

    override fun onCreate() {
        super.onCreate()
        LogFactory.configureLoggers(
            context = this,
            config = LogConfig(
                // Use custom log message formatter
                // formatter = CustomFormatter()
                // Clear the old logs every time app launched
                // clearFileWhenAppLaunched = true
            ),
            enabledLoggers = arrayOf(FileLogger())
        )
    }

}

class CustomFormatter : LogMessageFormatter {
    override fun format(logEntry: LogEntry) =
        "${logEntry.logType} \n ${logEntry.message} \n ${logEntry.date}"
}
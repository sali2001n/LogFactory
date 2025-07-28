package com.sali.logfactory

import android.app.Application
import com.sali.logfactory.factory.LogFactory
import com.sali.logfactory.formatter.LogMessageFormatter
import com.sali.logfactory.logger.EmailLogger
import com.sali.logfactory.logger.FileLogger
import com.sali.logfactory.models.FileLoggerConfig
import com.sali.logfactory.models.LogEntry
import com.sali.logfactory.models.SmtpConfig
import com.sali.logfactory.models.ThresholdType

class LogFactoryApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val emailLogger = EmailLogger(
            smtpConfig = SmtpConfig(
                thresholdType = ThresholdType.Counter, // Specify which type of threshold you want to use
                logCountThreshold = 3, // For ThresholdType.Counter type you can specify after how many logs should be sent to the email address
                // timeThresholdMillis = 5 * 1000 * 60, You can use time threshold if you choose ThresholdType.Timer
                senderEmail = "sender@gmail.com",
                senderPassword = "sender password",
                recipientEmail = "recepient@gmail.com",
            ),
            // Use custom log message formatter
            // formatter = CustomFormatter()
        )

        val fileLogger = FileLogger(
            config = FileLoggerConfig(
                // Use custom log message formatter
                // formatter = CustomFormatter()
                // Clear the old logs every time app launched
                // clearFileWhenAppLaunched = true
            )
        )

        LogFactory.configureLoggers(
            context = this,
            enabledLoggers = arrayOf(fileLogger, emailLogger)
        )
    }

}

class CustomFormatter : LogMessageFormatter {
    override fun format(logEntry: LogEntry) =
        "${logEntry.logType} \n ${logEntry.message} \n ${logEntry.date}"
}
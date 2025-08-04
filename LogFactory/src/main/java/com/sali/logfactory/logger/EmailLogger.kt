package com.sali.logfactory.logger

import android.content.Context
import android.util.Log
import com.sali.logfactory.formatter.DefaultLogMessageFormatter
import com.sali.logfactory.formatter.LogMessageFormatter
import com.sali.logfactory.models.EmailLoggerConfig
import com.sali.logfactory.models.LogEntry
import com.sali.logfactory.models.ThresholdType
import com.sali.logfactory.util.StorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Properties
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/**
 * EmailLogger is an implementation of [ILogger] that collects logs and sends them via email
 * through an SMTP server when a certain threshold is met. The logs are first written to a local file
 * and then attached as a file to the outgoing email.
 *
 * Supports two threshold types via [ThresholdType] in [EmailLoggerConfig]:
 * - [ThresholdType.Counter]: Sends logs when a certain number of log entries have been accumulated.
 * - [ThresholdType.Timer]: Sends logs periodically based on time interval.
 *
 * Initialization:
 * ```
 * LogFactory.configureLoggers(context, EmailLogger(emailLoggerConfig))
 * ```
 *
 * @property config Configuration for the SMTP server, sender, recipient, and thresholds.
 * @property formatter Formats the [LogEntry] into a string to be saved in the log file.
 */
class EmailLogger(
    private val config: EmailLoggerConfig,
    val formatter: LogMessageFormatter = DefaultLogMessageFormatter,
) : ILogger, LoggerInitializer {

    companion object {
        private const val EMAIL_LOGGER_TAG = "EmailLogger"
    }

    private lateinit var context: Context
    private var logCount = AtomicInteger(0)
    private var lastSentTime = AtomicLong(System.currentTimeMillis())
    private var lastFailedAttempt = AtomicLong(0L)
    private val retryDelayMillis = 5 * 60 * 1000L
    private val loggerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var isSending = false

    override fun initialize(
        context: Context,
    ) {
        this.context = context.applicationContext
    }

    override fun log(logEntry: LogEntry) {
        StorageManager.writeLogsToTheFile(context, formatter.format(logEntry))

        when (config.thresholdType) {
            ThresholdType.Counter -> {
                logCount.incrementAndGet()
                if (logCount.get() >= config.logCountThreshold) {
                    sendLogsViaSmtp { result, message ->
                        handleSendResult(
                            result = result,
                            message = message,
                            onSuccess = { logCount.set(0) }
                        )
                    }
                }
            }

            ThresholdType.Timer -> {
                val now = System.currentTimeMillis()
                if (now - lastSentTime.get() >= config.timeThresholdMillis && // Check for interval
                    now - lastFailedAttempt.get() >= retryDelayMillis // Check for failed attempts
                ) {
                    sendLogsViaSmtp { result, message ->
                        handleSendResult(
                            result = result,
                            message = message,
                            onSuccess = { lastFailedAttempt.set(now) },
                            onFailure = { lastFailedAttempt.set(now) }
                        )
                    }
                }
            }
        }

    }

    private fun handleSendResult(
        result: Boolean,
        message: String?,
        onSuccess: () -> Unit,
        onFailure: () -> Unit = {},
    ) {
        isSending = false
        if (result) {
            onSuccess()
        } else {
            onFailure()
            Log.e(EMAIL_LOGGER_TAG, "SMTP failed: $message")
        }
    }

    private fun sendLogsViaSmtp(onResult: (Boolean, String?) -> Unit) {
        if (!isSending) {
            isSending = true
            loggerScope.launch {
                try {
                    val logFile = StorageManager.getLogFile(context)
                    if (!logFile.exists() || logFile.length() == 0L) {
                        onResult(false, "Log file is empty.")
                        return@launch
                    }

                    val props = Properties().apply {
                        put("mail.smtp.auth", "true")
                        put(
                            "mail.smtp.starttls.enable",
                            if (config.useSSL) "true" else "false"
                        )
                        put("mail.smtp.host", config.smtpHost)
                        put("mail.smtp.port", config.smtpPort)
                    }

                    val session = Session.getInstance(props, object : Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication {
                            return PasswordAuthentication(
                                config.senderEmail,
                                config.senderPassword
                            )
                        }
                    })

                    val message = MimeMessage(session).apply {
                        setFrom(InternetAddress(config.senderEmail))
                        setRecipients(
                            Message.RecipientType.TO,
                            InternetAddress.parse(config.recipientEmail)
                        )
                        subject = "App Logs"

                        val multipart = MimeMultipart()
                        val textPart = MimeBodyPart().apply {
                            setText("Attached is the latest log file.")
                        }
                        multipart.addBodyPart(textPart)
                        val attachmentPart = MimeBodyPart().apply {
                            attachFile(logFile)
                        }
                        multipart.addBodyPart(attachmentPart)
                        setContent(multipart)
                    }

                    Transport.send(message)
                    StorageManager.clearLogFile(context)
                    onResult(true, null)
                } catch (e: Exception) {
                    onResult(false, e.message)
                }
            }
        }
    }
}
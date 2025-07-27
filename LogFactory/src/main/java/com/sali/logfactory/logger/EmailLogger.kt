package com.sali.logfactory.logger

import android.content.Context
import android.util.Log
import com.sali.logfactory.formatter.DefaultLogMessageFormatter
import com.sali.logfactory.formatter.LogMessageFormatter
import com.sali.logfactory.models.LogConfig
import com.sali.logfactory.models.LogEntry
import com.sali.logfactory.models.SmtpConfig
import com.sali.logfactory.models.ThresholdType
import com.sali.logfactory.util.StorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailLogger(
    private val smtpConfig: SmtpConfig,
    val formatter: LogMessageFormatter = DefaultLogMessageFormatter,
) : ILogger {

    companion object {
        private const val EMAIL_LOGGER_TAG = "EmailLogger"
    }

    private lateinit var context: Context
    private var logCount = 0
    private var lastSentTime = System.currentTimeMillis()
    private var lastFailedAttempt = 0L
    private val retryDelayMillis = 5 * 60 * 1000L

    @Volatile
    private var isSending = false

    override fun initialize(
        context: Context,
        config: LogConfig,
    ) {
        this.context = context.applicationContext
    }

    override fun log(logEntry: LogEntry) {
        StorageManager.writeLogsToTheFile(context, formatter.format(logEntry))

        when (smtpConfig.thresholdType) {
            ThresholdType.Counter -> {
                logCount++
                if (logCount >= smtpConfig.logCountThreshold) {
                    sendLogsViaSmtp { result, message ->
                        handleSendResult(
                            result = result,
                            message = message,
                            onSuccess = { logCount = 0 }
                        )
                    }
                }
            }

            ThresholdType.Timer -> {
                val now = System.currentTimeMillis()
                if (now - lastSentTime >= smtpConfig.timeThresholdMillis && // Check for interval
                    now - lastFailedAttempt >= retryDelayMillis // Check for failed attempts
                ) {
                    sendLogsViaSmtp { result, message ->
                        handleSendResult(
                            result = result,
                            message = message,
                            onSuccess = { lastFailedAttempt = now },
                            onFailure = { lastFailedAttempt = now }
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

    fun sendLogsViaSmtp(onResult: (Boolean, String?) -> Unit) {
        if (!isSending) {
            isSending = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val logFile = StorageManager.getLogFile(context)
                    if (!logFile.exists() || logFile.length() == 0L) {
                        onResult(false, "Log file is empty.")
                        return@launch
                    }

                    val props = Properties().apply {
                        put("mail.smtp.auth", "true")
                        put("mail.smtp.starttls.enable", if (smtpConfig.useSSL) "true" else "false")
                        put("mail.smtp.host", smtpConfig.host)
                        put("mail.smtp.port", smtpConfig.port)
                    }

                    val session = Session.getInstance(props, object : Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication {
                            return PasswordAuthentication(
                                smtpConfig.senderEmail,
                                smtpConfig.senderPassword
                            )
                        }
                    })

                    val message = MimeMessage(session).apply {
                        setFrom(InternetAddress(smtpConfig.senderEmail))
                        setRecipients(
                            Message.RecipientType.TO,
                            InternetAddress.parse(smtpConfig.recipientEmail)
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
package com.sali.logfactory.models

data class EmailLoggerConfig(
    val thresholdType: ThresholdType,
    val logCountThreshold: Int = 10,
    val timeThresholdMillis: Long = 5 * 60 * 1000L, // 5 minutes
    val smtpHost: String = "smtp.gmail.com",
    val smtpPort: Int = 587,
    val senderEmail: String,
    val senderPassword: String,
    val recipientEmail: String,
    val useSSL: Boolean = true,
)

enum class ThresholdType {
    Counter, Timer
}
package com.sali.logfactory.factory

import android.content.Context
import android.util.Log
import com.sali.logfactory.factory.LogFactory.configureLoggers
import com.sali.logfactory.factory.LogFactory.shutdown
import com.sali.logfactory.logger.FactoryInitializer
import com.sali.logfactory.logger.ILogger
import com.sali.logfactory.models.LogEntry
import com.sali.logfactory.models.LogType
import java.util.Date

/**
 * LogFactory is a singleton object that manages multiple loggers and provides a centralized
 * mechanism for logging messages in your application.
 *
 * You must call [configureLoggers] once (typically in Application.onCreate) to initialize
 * the system with your desired loggers (e.g., FileLogger, EmailLogger).
 *
 * Usage:
 * ```
 * // Initialize in Application class
 * LogFactory.configureLoggers(applicationContext, FileLogger(config))
 *
 * // Log a message
 * LogFactory.log(LogType.ERROR, "MyTag", "Something went wrong", exception)
 * ```
 *
 * Key Features:
 * - Routes logs to all active [ILogger] implementations
 * - Allows graceful shutdown and reinitialization via [shutdown]
 */
object LogFactory {

    private const val LOG_FACTORY_TAG = "LogFactory"
    private var isConfigured = false // Renamed for clarity, it means LogFactory's setup is done
    private val loggers = mutableListOf<ILogger>()

    /**
     * Initializes the logging system with specified loggers.
     * This method should be called once, typically in your Application's onCreate.
     *
     * @param context Application context, used for file operations (MediaStore).
     * @param enabledLoggers A vararg list of ILogger instances to enable.
     */
    fun configureLoggers(
        context: Context,
        vararg enabledLoggers: ILogger,
    ) {
        if (isConfigured) {
            Log.w(LOG_FACTORY_TAG, "LogFactory already configured.")
            return
        }

        loggers.clear()

        enabledLoggers.forEach { logger ->
            try {
                if (logger is FactoryInitializer)
                    logger.initialize(context)

                loggers.add(logger)
            } catch (e: Exception) {
                Log.e(
                    LOG_FACTORY_TAG,
                    "Failed to initialize logger: ${logger::class.simpleName}",
                    e
                )
            }
        }

        isConfigured = true
        Log.i(LOG_FACTORY_TAG, "LogFactory configured with ${loggers.size} loggers.")
    }

    /**
     * Logs a message with the specified parameters.
     *
     * @param logType The type of log (e.g., DEBUG, ERROR, INFO)
     * @param tag The log tag, usually indicating the caller (e.g., class name)
     * @param message The message to log
     * @param throwable Optional exception or error to include with the log
     */
    fun log(
        logType: LogType,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        if (!isConfigured) {
            Log.e(LOG_FACTORY_TAG, "LogFactory not configured! Call configureLoggers() first.")
            return
        }

        val logEntry = LogEntry(
            logType = logType,
            date = Date(),
            tag = tag,
            message = message,
            throwable = throwable
        )

        loggers.forEach { logger ->
            logger.log(logEntry)
        }
    }

    /**
     * Shuts down the logging system and clears all configured loggers.
     * Use this to release resources or reset the logger system.
     */
    fun shutdown() {
        loggers.clear()
        isConfigured = false
        Log.i(LOG_FACTORY_TAG, "LogFactory shutdown.")
    }

}
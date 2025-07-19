package com.sali.logfactory.factory

import android.content.Context
import android.util.Log
import com.sali.logfactory.logger.ILogger
import com.sali.logfactory.models.LogConfig
import com.sali.logfactory.models.LogEntry
import com.sali.logfactory.models.LogType
import java.util.Date

object LogFactory {

    private const val LOG_FACTORY_TAG = "LogFactory"
    private var isConfigured = false // Renamed for clarity, it means LogFactory's setup is done
    private val loggers = mutableListOf<ILogger>()

    /**
     * Initializes the logging system with specified loggers.
     * This method should be called once, typically in your Application's onCreate.
     *
     * @param context Application context, used for file operations (MediaStore).
     * @param config Configuration for file logging (paths, filename).
     * @param enabledLoggers A vararg list of ILogger instances to enable.
     */
    fun configureLoggers(
        context: Context,
        config: LogConfig = LogConfig(),
        vararg enabledLoggers: ILogger,
    ) {
        if (isConfigured) {
            Log.w(LOG_FACTORY_TAG, "LogFactory already configured.")
            return
        }

        loggers.clear()

        enabledLoggers.forEach { logger ->
            try {
                logger.initialize(context, config)
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

    fun shutdown() {
        loggers.clear()
        isConfigured = false
        Log.i(LOG_FACTORY_TAG, "LogFactory shutdown.")
    }

}
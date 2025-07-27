package com.sali.logfactory.util

import android.content.Context
import java.io.File

object StorageManager {

    private const val LOG_FILE_NAME = "logs.txt"

    fun getLogFile(context: Context) = File(context.filesDir, LOG_FILE_NAME)

    fun writeLogsToTheFile(context: Context, logs: String) {
        val logFile = File(context.filesDir, LOG_FILE_NAME)
        logFile.appendText(logs)
    }

    fun clearLogFile(context: Context) {
        val file = getLogFile(context)
        if (file.exists()) {
            file.writeText("")
        }
    }

}
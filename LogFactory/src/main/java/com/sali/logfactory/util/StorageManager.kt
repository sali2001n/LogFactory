package com.sali.logfactory.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object StorageManager {

    private const val LOG_FILE_NAME = "logs.txt"
    private const val STORAGE_MANAGER_TAG = "EmailLogger"

    fun getLogFile(context: Context) = File(context.filesDir, LOG_FILE_NAME)

    fun writeLogsToTheFile(context: Context, logs: String) {
        val logFile = File(context.filesDir, LOG_FILE_NAME)
        logFile.appendText(logs)
    }

    fun clearLogFile(context: Context) {
        val file = getLogFile(context)
        if (file.exists()) {
            try {
                FileOutputStream(file).channel.use { it.truncate(0) }
            } catch (e: IOException) {
                Log.e(STORAGE_MANAGER_TAG, "Failed to clear log file: ${e.message}")
            }
        }
    }

}
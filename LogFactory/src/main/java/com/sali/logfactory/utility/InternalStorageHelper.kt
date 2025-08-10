package com.sali.logfactory.utility

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object InternalStorageHelper {

    private const val LOG_FILE_NAME = "logs.txt"
    private const val STORAGE_MANAGER_TAG = "InternalStorageManager"

    internal fun getLogFileFromInternalStorage(context: Context) = File(context.filesDir, LOG_FILE_NAME)

    internal fun clearInternalStorageLogFile(context: Context) {
        val file = getLogFileFromInternalStorage(context)
        if (file.exists()) {
            try {
                FileOutputStream(file).channel.use { it.truncate(0) }
            } catch (e: IOException) {
                Log.e(STORAGE_MANAGER_TAG, "Failed to clear log file: ${e.message}")
            }
        }
    }

}
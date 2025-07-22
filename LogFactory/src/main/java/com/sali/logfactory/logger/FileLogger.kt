package com.sali.logfactory.logger

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.sali.logfactory.models.LogConfig
import com.sali.logfactory.models.LogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class FileLogger : ILogger {

    companion object {
        private const val FILE_LOGGER_TAG = "FileLogger"
    }

    private var logFile: File? = null
    private var logConfig: LogConfig? = null
    private var appContext: Context? = null

    override fun initialize(context: Context, config: LogConfig) {
        this.appContext = context.applicationContext
        this.logConfig = config

        if (isExternalStorageWritable()) {
            val directory =
                Environment.getExternalStoragePublicDirectory(config.parentDirectoryPath)
            val logDir = File(directory, config.childDirectoryPath)
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            logFile = File(logDir, config.fileName)
            Log.i(
                FILE_LOGGER_TAG,
                "FileLogger initialized. Log file path: ${logFile?.absolutePath}"
            )
        } else {
            Log.e(FILE_LOGGER_TAG, "External storage not writable for FileLogger.")
        }
    }

    override fun log(logEntry: LogEntry) {
        if (!isInitialized()) {
            Log.e(FILE_LOGGER_TAG, "FileLogger not initialized! Call initialize() first.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val currentLogConfig = logConfig!!
            val currentLogFile = logFile!!
            val currentAppContext = appContext!!

            val formattedMessage = currentLogConfig.formatter.format(logEntry)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val resolver = currentAppContext.contentResolver
                    val contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

                    val mediaStoreRelativePath =
                        "${currentLogConfig.parentDirectoryPath}/${currentLogConfig.childDirectoryPath}/"

                    val selection =
                        "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND ${MediaStore.MediaColumns.DISPLAY_NAME}=?"
                    val selectionArgs = arrayOf(mediaStoreRelativePath, currentLogConfig.fileName)
                    var uri: Uri? = null

                    resolver.query(contentUri, null, selection, selectionArgs, null)
                        ?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val id =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                                uri = ContentUris.withAppendedId(contentUri, id)
                            }
                        }

                    if (uri == null) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, currentLogConfig.fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, mediaStoreRelativePath)
                        }
                        uri = resolver.insert(contentUri, contentValues)
                    }

                    uri?.let { fileUri ->
                        resolver.openOutputStream(fileUri, "wa")?.use { outputStream ->
                            outputStream.write(formattedMessage.toByteArray())
                            outputStream.write("\n\n".toByteArray())
                        }
                    } ?: run {
                        Log.e(
                            FILE_LOGGER_TAG,
                            "Failed to get or create MediaStore URI for log file."
                        )
                    }
                } catch (e: Exception) {
                    Log.e(FILE_LOGGER_TAG, "Error writing to log file via MediaStore", e)
                }
            } else {
                try {
                    val writer = FileWriter(currentLogFile, true)
                    writer.use {
                        it.append(formattedMessage)
                        it.append("\n\n")
                        it.flush()
                    }
                } catch (e: Exception) {
                    Log.e(FILE_LOGGER_TAG, "Error writing to log file (old SDK)", e)
                }
            }
        }
    }

    private fun isInitialized(): Boolean =
        logFile != null && logConfig != null && appContext != null

    private fun isExternalStorageWritable() =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}
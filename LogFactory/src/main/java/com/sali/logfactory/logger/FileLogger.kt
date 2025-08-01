package com.sali.logfactory.logger

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.sali.logfactory.formatter.DefaultLogMessageFormatter
import com.sali.logfactory.formatter.LogMessageFormatter
import com.sali.logfactory.models.FileLoggerConfig
import com.sali.logfactory.models.LogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileWriter

/**
 * A file-based logger implementation that writes formatted log entries to a file,
 * supporting both legacy and scoped storage APIs (pre and post Android Q).
 *
 * This logger supports clearing the file on app launch and handles concurrent access.
 *
 * Initialization:
 * ```
 * LogFactory.configureLoggers(context, FileLogger(fileLoggerConfig))
 * ```
 *
 * @property config Configuration for file logging (e.g., file name, formatter, directories, and flags).
 * @property formatter Formats the [LogEntry] into a string to be saved in the log file.
 */
class FileLogger(
    val config: FileLoggerConfig,
    val formatter: LogMessageFormatter = DefaultLogMessageFormatter,
) : ILogger {

    companion object {
        private const val FILE_LOGGER_TAG = "FileLogger"
    }

    private var logFile: File? = null
    private var appContext: Context? = null

    private val clearLogsFileMutex = Mutex()
    private var isFileClearedThisSession = false

    override fun initialize(context: Context) {
        this.appContext = context.applicationContext

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
            val currentLogFile = logFile!!
            val currentAppContext = appContext!!

            val formattedMessage = formatter.format(logEntry)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                loggingMechanismForApiVersion29AndAbove(
                    currentAppContext = currentAppContext,
                    formattedMessage = formattedMessage
                )
            } else {
                loggingMechanismForApiVersionBelow29(
                    currentLogFile = currentLogFile,
                    formattedMessage = formattedMessage
                )
            }
        }
    }

    private fun deleteOldLogsFile(
        resolver: ContentResolver,
        contentUri: Uri,
        selection: String,
        selectionArgs: Array<String>,
    ) {
        resolver.query(
            contentUri,
            arrayOf(MediaStore.MediaColumns._ID),
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                val uriToDelete = ContentUris.withAppendedId(contentUri, id)
                try {
                    resolver.delete(uriToDelete, null, null)
                    Log.d(
                        FILE_LOGGER_TAG,
                        "Existing log file deleted successfully."
                    )
                } catch (e: Exception) {
                    Log.e(
                        FILE_LOGGER_TAG,
                        "Error deleting existing log file.",
                        e
                    )
                }
            }
        }
        isFileClearedThisSession = true
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun loggingMechanismForApiVersion29AndAbove(
        currentAppContext: Context,
        formattedMessage: String,
    ) {
        try {
            val resolver = currentAppContext.contentResolver
            val contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

            val mediaStoreRelativePath =
                "${config.parentDirectoryPath}/${config.childDirectoryPath}/"

            val selection =
                "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND ${MediaStore.MediaColumns.DISPLAY_NAME}=?"
            val selectionArgs = arrayOf(mediaStoreRelativePath, config.fileName)

            if (config.clearFileWhenAppLaunched) {
                clearLogsFileMutex.withLock {
                    if (!isFileClearedThisSession) {
                        deleteOldLogsFile(
                            resolver = resolver,
                            contentUri = contentUri,
                            selection = selection,
                            selectionArgs = selectionArgs
                        )
                        isFileClearedThisSession = true
                    }
                }
            }

            // Find or create URI
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
                    put(MediaStore.MediaColumns.DISPLAY_NAME, config.fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, mediaStoreRelativePath)
                }
                uri = resolver.insert(contentUri, contentValues)
            }

            uri?.let { fileUri ->
                resolver.openOutputStream(fileUri, "wa")?.use { outputStream ->
                    outputStream.write(formattedMessage.toByteArray())
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
    }

    private suspend fun loggingMechanismForApiVersionBelow29(
        currentLogFile: File,
        formattedMessage: String,
    ) {
        try {
            if (config.clearFileWhenAppLaunched) {
                clearLogsFileMutex.withLock {
                    if (!isFileClearedThisSession) {
                        val deleteResult = currentLogFile.delete()
                        if (!deleteResult)
                            Log.e(FILE_LOGGER_TAG, "Deletion failed for APIs 29<")
                        isFileClearedThisSession = true
                        true
                    } else false
                }
            } else false

            val writer = FileWriter(currentLogFile, true)
            writer.use {
                it.append(formattedMessage)
                it.flush()
            }
        } catch (e: Exception) {
            Log.e(FILE_LOGGER_TAG, "Error writing to log file (old SDK)", e)
        }
    }

    private fun isInitialized(): Boolean =
        logFile != null && appContext != null

    private fun isExternalStorageWritable() =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}
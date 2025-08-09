package com.sali.logfactory.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object StorageManager {

    private const val LOG_FILE_NAME = "logs.txt"
    private const val STORAGE_MANAGER_TAG = "StorageManager"

    fun getLogFileFromInternalStorage(context: Context) = File(context.filesDir, LOG_FILE_NAME)

    fun writeLogsToInternalStorageLogFile(context: Context, logs: String) {
        val logFile = getLogFileFromInternalStorage(context)
        logFile.appendText(logs)
    }

    fun clearInternalStorageLogFile(context: Context) {
        val file = getLogFileFromInternalStorage(context)
        if (file.exists()) {
            try {
                FileOutputStream(file).channel.use { it.truncate(0) }
            } catch (e: IOException) {
                Log.e(STORAGE_MANAGER_TAG, "Failed to clear log file: ${e.message}")
            }
        }
    }

    fun deleteFileFromMediaStore(
        context: Context,
        contentUri: Uri,
        relativePath: String,
        fileName: String,
    ): Boolean {
        val resolver = context.contentResolver
        val selection =
            "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND ${MediaStore.MediaColumns.DISPLAY_NAME}=?"
        val selectionArgs = arrayOf(relativePath, fileName)

        return try {
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
                    resolver.delete(uriToDelete, null, null)
                    Log.d(STORAGE_MANAGER_TAG, "Deleted file $fileName in $relativePath")
                    true
                } else {
                    false
                }
            } ?: false
        } catch (e: Exception) {
            Log.e(STORAGE_MANAGER_TAG, "Failed to delete file: ${e.message}")
            false
        }
    }

    fun isExternalStorageWritable() =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

}
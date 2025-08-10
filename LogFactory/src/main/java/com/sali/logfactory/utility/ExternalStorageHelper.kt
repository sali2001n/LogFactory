package com.sali.logfactory.utility

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log

object ExternalStorageHelper {

    private const val STORAGE_MANAGER_TAG = "ExternalStorageManager"

    internal fun findFileUri(
        resolver: ContentResolver,
        contentUri: Uri,
        selection: String,
        selectionArgs: Array<String>,
        onFindUri: (Uri?) -> Unit,
    ) {
        resolver.query(contentUri, null, selection, selectionArgs, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    val uri = ContentUris.withAppendedId(contentUri, id)
                    onFindUri(uri)
                }
            }
    }

    internal fun createFileUri(
        fileName: String,
        mediaStoreRelativePath: String,
        resolver: ContentResolver,
        contentUri: Uri,
        onCreateUri: (Uri?) -> Unit,
    ) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, mediaStoreRelativePath)
        }
        val uri = resolver.insert(contentUri, contentValues)
        onCreateUri(uri)
    }

    internal fun writeToLogFile(uri: Uri?, resolver: ContentResolver, message: String) {
        uri?.let { fileUri ->
            resolver.openOutputStream(fileUri, "wa")?.use { outputStream ->
                outputStream.write(message.toByteArray())
            }
        } ?: run {
            Log.e(
                STORAGE_MANAGER_TAG,
                "Failed to get or create MediaStore URI for log file."
            )
        }
    }

    internal fun deleteFileFromMediaStore(
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

    internal fun isExternalStorageWritable() =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

}
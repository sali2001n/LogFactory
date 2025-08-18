package com.sali.logfactory.utility

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.sali.logfactory.factory.LibraryTag

object ExternalStorageHelper {

    private const val LOG_TAG = "${LibraryTag.TAG}/ExternalStorageManager"

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

    internal fun deleteFileFromMediaStore(
        resolver: ContentResolver,
        selection: String,
        selectionArgs: Array<String>,
        contentUri: Uri,
        relativePath: String,
        fileName: String,
    ): Boolean {
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
                    Log.d(LOG_TAG, "Deleted file $fileName in $relativePath")
                    true
                } else {
                    false
                }
            } ?: false
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to delete file: ${e.message}")
            false
        }
    }

    internal fun isExternalStorageWritable() =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

}
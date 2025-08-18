package com.sali.logfactory.utility

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.sali.logfactory.factory.LibraryTag
import java.io.File
import java.io.FileWriter

object FileHelper {

    private const val LOG_TAG = "${LibraryTag.TAG}/FileHelper"

    internal fun writeToLogFile(uri: Uri?, resolver: ContentResolver, message: String) {
        uri?.let { fileUri ->
            resolver.openOutputStream(fileUri, "wa")?.use { outputStream ->
                outputStream.write(message.toByteArray())
            }
        } ?: run {
            Log.e(
                LOG_TAG,
                "Failed to get or create MediaStore URI for log file."
            )
        }
    }

    internal fun writeLogToFile(file: File, message: String) {
        val writer = FileWriter(file, true)
        writer.use {
            it.append(message)
            it.flush()
        }
    }


}
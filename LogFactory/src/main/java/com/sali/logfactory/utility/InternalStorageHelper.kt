package com.sali.logfactory.utility

import android.content.Context
import java.io.File

object InternalStorageHelper {

    private const val LOG_FILE_NAME = "logs.txt"

    internal fun getLogFile(context: Context) =
        File(context.filesDir, LOG_FILE_NAME)

}
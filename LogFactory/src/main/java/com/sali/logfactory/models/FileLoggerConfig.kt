package com.sali.logfactory.models

import android.os.Environment
import com.sali.logfactory.formatter.DefaultLogMessageFormatter
import com.sali.logfactory.formatter.LogMessageFormatter

data class FileLoggerConfig(
    val parentDirectoryPath: String = Environment.DIRECTORY_DOWNLOADS,
    val childDirectoryPath: String = "MyAppLogs",
    val fileName: String = "my_app_log.txt",
    val clearFileWhenAppLaunched: Boolean = false,
    val formatter: LogMessageFormatter = DefaultLogMessageFormatter,
)

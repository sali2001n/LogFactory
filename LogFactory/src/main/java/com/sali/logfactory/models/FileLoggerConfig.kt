package com.sali.logfactory.models

import android.os.Environment

data class FileLoggerConfig(
    val parentDirectoryPath: String = Environment.DIRECTORY_DOWNLOADS,
    val childDirectoryPath: String = "MyAppLogs",
    val fileName: String = "my_app_log.txt",
    val clearFileWhenAppLaunched: Boolean = false,
)

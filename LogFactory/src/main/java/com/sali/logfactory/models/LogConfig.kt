package com.sali.logfactory.models

import android.os.Environment

data class LogConfig(
    val parentDirectoryPath: String = Environment.DIRECTORY_DOWNLOADS,
    val childDirectoryPath: String = "MyAppLogs",
    val fileName: String = "my_app_log.txt",
)

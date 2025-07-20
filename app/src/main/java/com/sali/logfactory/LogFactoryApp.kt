package com.sali.logfactory

import android.app.Application
import com.sali.logfactory.factory.LogFactory
import com.sali.logfactory.logger.FileLogger
import com.sali.logfactory.models.LogConfig

class LogFactoryApp : Application() {

    override fun onCreate() {
        super.onCreate()
        LogFactory.configureLoggers(
            context = this,
            config = LogConfig(),
            enabledLoggers = arrayOf(FileLogger())
        )
    }

}
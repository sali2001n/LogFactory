package com.sali.logfactory.logger

import android.content.Context

interface FactoryInitializer {

    /**
     * Initializes the logger.
     * This method should be called before loggers that need it.
     */
    fun initialize(context: Context)

}
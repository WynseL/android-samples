package com.chadgee.location

import android.app.Application
import timber.log.Timber

class ThisApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}
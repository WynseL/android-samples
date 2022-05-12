package com.chadgee.concepts

import android.app.Application
import timber.log.Timber

class ThisApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}
package com.example.runtracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        // this is a logging library just like the "log.i()".
        // we are writing it here cause we would be using it throughout the entire application
        Timber.plant(Timber.DebugTree())
    }
}
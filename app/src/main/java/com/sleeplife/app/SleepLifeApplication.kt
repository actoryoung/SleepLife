package com.sleeplife.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SleepLifeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}

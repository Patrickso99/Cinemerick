package com.preichert.cinemerick

import android.app.Application
import org.koin.android.ext.koin.androidContext

class CinemerickApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@CinemerickApplication)
        }
    }
}

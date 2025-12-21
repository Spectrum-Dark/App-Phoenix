package com.spectrum.phoenix

import android.app.Application
import android.content.Context

class PhoenixApp : Application() {
    companion object {
        lateinit var instance: PhoenixApp
            private set
        
        val context: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

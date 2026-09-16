package com.hcs.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.hcs.push.PushEngineManager

class HcsPushGmsService : Service() {
    private lateinit var pushManager: PushEngineManager
    private lateinit var binder: PushBinder

    override fun onCreate() {
        super.onCreate()
        pushManager = PushEngineManager(applicationContext)
        binder = PushBinder(pushManager)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}

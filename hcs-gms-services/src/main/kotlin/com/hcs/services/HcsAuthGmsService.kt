package com.hcs.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.hcs.auth.HcsAuthClient

class HcsAuthGmsService : Service() {
    private lateinit var authClient: HcsAuthClient
    private lateinit var binder: AuthBinder

    override fun onCreate() {
        super.onCreate()
        authClient = HcsAuthClient(applicationContext)
        binder = AuthBinder(authClient)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}

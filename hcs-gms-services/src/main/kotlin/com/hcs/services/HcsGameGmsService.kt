package com.hcs.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.hcs.games.HcsGameServicesClient

class HcsGameGmsService : Service() {
    private lateinit var gameClient: HcsGameServicesClient
    private lateinit var binder: GameBinder

    override fun onCreate() {
        super.onCreate()
        gameClient = HcsGameServicesClient(applicationContext)
        binder = GameBinder(gameClient)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}

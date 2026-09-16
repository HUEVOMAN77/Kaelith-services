package com.hcs.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.hcs.location.HcsFusedLocationProviderClient

class HcsLocationGmsService : Service() {
    private lateinit var locationClient: HcsFusedLocationProviderClient
    private lateinit var binder: LocationBinder

    override fun onCreate() {
        super.onCreate()
        locationClient = HcsFusedLocationProviderClient(applicationContext)
        binder = LocationBinder(locationClient)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}

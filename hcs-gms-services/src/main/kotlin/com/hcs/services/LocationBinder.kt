package com.hcs.services

import android.os.Binder
import android.os.Bundle
import android.os.Parcel
import org.hcs.location.HcsFusedLocationProviderClient
import org.hcs.location.HcsLocation
import org.hcs.location.LocationCallback
import org.hcs.location.LocationRequest
import org.hcs.tasks.Tasks
import java.util.concurrent.TimeUnit

class LocationBinder(
    private val locationClient: HcsFusedLocationProviderClient
) : Binder() {

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (reply == null) {
            return super.onTransact(code, data, reply, flags)
        }
        return when (code) {
            FIRST_CALL_TRANSACTION -> {
                val locTask = locationClient.getLastLocation()
                val loc = try {
                    Tasks.await(locTask, 1, TimeUnit.SECONDS)
                } catch (e: Exception) {
                    null
                }

                reply.writeNoException()
                if (loc != null) {
                    reply.writeInt(1)
                    val bundle = Bundle().apply {
                        putDouble("latitude", loc.latitude)
                        putDouble("longitude", loc.longitude)
                        putFloat("accuracy", loc.accuracy)
                        putLong("time", loc.time)
                    }
                    bundle.writeToParcel(reply, 0)
                } else {
                    reply.writeInt(0)
                }
                true
            }
            FIRST_CALL_TRANSACTION + 1 -> {
                val interval = if (data.dataAvail() > 0) data.readLong() else 5000L
                val req = LocationRequest(intervalMs = interval)
                val task = locationClient.requestLocationUpdates(req, object : LocationCallback() {}, null)
                reply.writeNoException()
                reply.writeInt(if (task.isSuccessful) 1 else 0)
                true
            }
            else -> super.onTransact(code, data, reply, flags)
        }
    }

    companion object {
        const val DESCRIPTOR = "com.google.android.gms.location.internal.IGoogleLocationManagerService"
    }
}

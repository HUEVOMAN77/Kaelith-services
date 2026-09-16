package org.hcs.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

data class LocationRequest(
    val priority: Int = PRIORITY_HIGH_ACCURACY,
    val intervalMs: Long = 5000L,
    val fastestIntervalMs: Long = 2000L,
    val smallestDisplacementMeters: Float = 0f
) {
    companion object {
        const val PRIORITY_HIGH_ACCURACY = 100
        const val PRIORITY_BALANCED_POWER_ACCURACY = 102
        const val PRIORITY_LOW_POWER = 104
        const val PRIORITY_NO_POWER = 105
    }
}

abstract class LocationCallback {
    open fun onLocationResult(result: HcsLocationResult) {}
    open fun onLocationAvailability(available: Boolean) {}
}

interface FusedLocationProviderClient {
    fun getLastLocation(): Task<HcsLocation>
    fun requestLocationUpdates(request: LocationRequest, callback: LocationCallback, looper: Looper?): Task<Void?>
    fun removeLocationUpdates(callback: LocationCallback): Task<Void?>
}

class HcsFusedLocationProviderClient(
    private val context: Context
) : FusedLocationProviderClient {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val registeredListeners = mutableMapOf<LocationCallback, LocationListener>()

    @SuppressLint("MissingPermission")
    override fun getLastLocation(): Task<HcsLocation> {
        val tcs = TaskCompletionSource<HcsLocation>()
        try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null

            for (provider in providers) {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }

            if (bestLocation != null) {
                tcs.setResult(
                    HcsLocation(
                        provider = bestLocation.provider ?: "unknown",
                        latitude = bestLocation.latitude,
                        longitude = bestLocation.longitude,
                        accuracy = bestLocation.accuracy,
                        time = bestLocation.time
                    )
                )
            } else {
                tcs.setResult(HcsLocation("hcs_mock_provider", 0.0, 0.0, 100f))
            }
        } catch (e: Exception) {
            tcs.setException(e)
        }
        return tcs.task
    }

    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(
        request: LocationRequest,
        callback: LocationCallback,
        looper: Looper?
    ): Task<Void?> {
        val tcs = TaskCompletionSource<Void?>()
        try {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val hcsLoc = HcsLocation(
                        provider = location.provider ?: "gps",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        time = location.time
                    )
                    callback.onLocationResult(HcsLocationResult(listOf(hcsLoc)))
                }

                @Deprecated("Deprecated in API 29")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {
                    callback.onLocationAvailability(true)
                }
                override fun onProviderDisabled(provider: String) {
                    callback.onLocationAvailability(false)
                }
            }

            val provider = when (request.priority) {
                LocationRequest.PRIORITY_HIGH_ACCURACY -> LocationManager.GPS_PROVIDER
                else -> LocationManager.NETWORK_PROVIDER
            }

            val targetProvider = if (locationManager.isProviderEnabled(provider)) {
                provider
            } else {
                LocationManager.PASSIVE_PROVIDER
            }

            registeredListeners[callback] = listener
            locationManager.requestLocationUpdates(
                targetProvider,
                request.intervalMs,
                request.smallestDisplacementMeters,
                listener,
                looper ?: Looper.getMainLooper()
            )
            tcs.setResult(null)
        } catch (e: Exception) {
            tcs.setException(e)
        }
        return tcs.task
    }

    override fun removeLocationUpdates(callback: LocationCallback): Task<Void?> {
        val tcs = TaskCompletionSource<Void?>()
        try {
            val listener = registeredListeners.remove(callback)
            if (listener != null) {
                locationManager.removeUpdates(listener)
            }
            tcs.setResult(null)
        } catch (e: Exception) {
            tcs.setException(e)
        }
        return tcs.task
    }
}
